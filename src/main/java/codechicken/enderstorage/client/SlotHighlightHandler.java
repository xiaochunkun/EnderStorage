package codechicken.enderstorage.client;

import codechicken.enderstorage.tile.TileEnderChest;
import codechicken.enderstorage.tile.TileEnderTank;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static codechicken.lib.vec.Vector3.CENTER;

/**
 * Renders a white outline over the hovered slot square on EnderChest/EnderTank.
 */
public class SlotHighlightHandler {

    // Highlight style constants (change here to tweak color/opacities)
    public static final float H_R = 1.0F; // red
    public static final float H_G = 1.0F; // green
    public static final float H_B = 1.0F; // blue
    public static final float H_A = 0.6F; // alpha (semi-transparent by default)
    public static final double H_INFLATE = 0.0025; // outline expand to avoid z-fighting
    public static final double H_SCALE = 1.02; // scale relative to slot square (sync by default)

    @SubscribeEvent
    public static void onRenderHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Recompute with CCL RayTracer to obtain subHit information.
        var retrace = RayTracer.retrace(mc.player);
        if (!(retrace instanceof SubHitBlockHitResult sub)) {
            return; // Not our traced type.
        }
        if (!(event.getTarget() instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult bhr = event.getTarget();
        BlockPos pos = bhr.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof TileFrequencyOwner)) {
            return;
        }

        int slotIndex = sub.subHit - 1; // 0..2 when hovering slot squares.
        if (slotIndex < 0 || slotIndex > 2) {
            return; // Only customize when pointing at a slot.
        }

        // Build the hovered slot's cuboid in world space.
        // Base square box per block type (bottom anchored like VoxelShape definitions).
        double hh;
        double hY;
        if (be instanceof TileEnderChest) {
            hh = TileFrequencyOwner.CHEST_SLOT_HALF;
            hY = TileFrequencyOwner.CHEST_SLOT_HEIGHT;
        } else {
            hh = TileFrequencyOwner.TANK_SLOT_HALF;
            hY = TileFrequencyOwner.TANK_SLOT_HEIGHT;
        }
        Cuboid6 box = new Cuboid6(-hh, 0, -hh, hh, hY, hh);

        if (be instanceof TileEnderChest chest) {
            // Chest local bottom Y on top surface ~14/16
            double y = 14D / 16D + 0.001D;
            double x = slotIndex == 0 ? -3D / 16D : (slotIndex == 1 ? 3D / 16D : 0D);
            double z = slotIndex == 2 ? 2D / 16D : -2D / 16D;
            // Move to center, rotate around center according to chest.rotation
            box.apply(new codechicken.lib.vec.Translation(0.5 + x, y, 0.5 + z));
            box.apply(new Rotation((-90 * (chest.rotation)) * MathHelper.torad, Vector3.Y_POS).at(new Vector3(0.5, y, 0.5)));
        } else if (be instanceof TileEnderTank tank) {
            // Tank local bottom Y on top surface ~0.91
            double y = 0.91 + 0.001;
            double cx = (slotIndex == 0 ? 0.40 : (slotIndex == 1 ? 0.60 : 0.50));
            double cz = (slotIndex == 2 ? 0.58 : 0.42);
            // Build relative vector from center and rotate it matching render order.
            double rx = cx - 0.5;
            double rz = cz - 0.5;
            int k = (tank.rotation + 2) & 3; // steps of -90 deg
            switch (k) {
                case 0 -> {
                    // no-op
                }
                case 1 -> {
                    double nx = rz; rz = -rx; rx = nx; // -90 deg: (x,z) -> (z,-x)
                }
                case 2 -> {
                    rx = -rx; rz = -rz; // -180
                }
                case 3 -> {
                    double nx = -rz; rz = rx; rx = nx; // -270: (x,z) -> (-z,x)
                }
            }
            box.apply(new codechicken.lib.vec.Translation(0.5 + rx, y, 0.5 + rz));
        } else {
            return;
        }

        // Move to world space.
        box.apply(new codechicken.lib.vec.Translation(pos.getX(), pos.getY(), pos.getZ()));

        // Draw a white outline around the slot square.
        PoseStack ps = event.getPoseStack();
        VertexConsumer vc = event.getMultiBufferSource().getBuffer(RenderType.lines());
        Vec3 cam = event.getCamera().getPosition();

        AABB base = new AABB(box.min.x, box.min.y, box.min.z, box.max.x, box.max.y, box.max.z);
        // Scale about center to enlarge the visible outline independently of hitbox size.
        double cx = (base.minX + base.maxX) * 0.5;
        double cy = (base.minY + base.maxY) * 0.5;
        double cz = (base.minZ + base.maxZ) * 0.5;
        double hx = (base.maxX - base.minX) * 0.5 * H_SCALE;
        double hv = (base.maxY - base.minY) * 0.5 * H_SCALE;
        double hz = (base.maxZ - base.minZ) * 0.5 * H_SCALE;
        AABB aabb = new AABB(cx - hx, cy - hv, cz - hz, cx + hx, cy + hv, cz + hz)
                .inflate(H_INFLATE);
        AABB rel = aabb.move(-cam.x, -cam.y, -cam.z);
        LevelRenderer.renderLineBox(ps, vc, rel, H_R, H_G, H_B, H_A);

        // Cancel vanilla highlight for this tick so only our outline shows.
        event.setCanceled(true);
    }
}
