package codechicken.enderstorage.client.render.tile;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.client.render.RenderCustomEndPortal;
import codechicken.enderstorage.tile.TileEnderChest;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModelLibrary;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

/**
 * Created by covers1624 on 4/12/2016.
 */
public class RenderTileEnderChest implements BlockEntityRenderer<TileEnderChest> {

    private static final RenderType chestType = RenderType.entityCutout(new ResourceLocation("enderstorage:textures/enderchest.png"));
    // private static final RenderType buttonType = RenderType.entitySolid(new ResourceLocation("enderstorage:textures/buttons.png"));
    private static final RenderType pearlType = CCModelLibrary.getIcos4RenderType(new ResourceLocation("enderstorage:textures/hedronmap.png"));
    private static final RenderCustomEndPortal renderEndPortal = new RenderCustomEndPortal(0.626, 0.188, 0.812, 0.188, 0.812);

    private final ModelPart bottom;
    private final ModelPart lid;
    private final ModelPart lock;
    private final ModelPart diamondLock;

    public RenderTileEnderChest(BlockEntityRendererProvider.Context context) {
        ModelPart chestRoot = createChestLayer().bakeRoot();
        bottom = chestRoot.getChild("bottom");
        lid = chestRoot.getChild("lid");
        lock = chestRoot.getChild("lock");
        diamondLock = chestRoot.getChild("diamond_lock");
    }

    public static LayerDefinition createChestLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 0.0F, 14, 10, 14), PartPose.offset(1.0F, 6F, 1.0F));
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -5F, -14F, 14, 5, 14), PartPose.offset(1.0F, 7F, 15F));
        root.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(-1F, -2F, -15F, 2, 4, 1), PartPose.offset(8F, 7F, 15F));
        root.addOrReplaceChild("diamond_lock", CubeListBuilder.create().texOffs(0, 5).addBox(-1F, -2F, -15F, 2, 4, 1), PartPose.offset(8F, 7F, 15F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(TileEnderChest enderChest, float partialTicks, PoseStack mStack, MultiBufferSource getter, int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        renderChest(ccrs, mStack, getter, enderChest.rotation, enderChest.getFrequency(), (float) enderChest.getRadianLidAngle(partialTicks), RenderUtils.getTimeOffset(enderChest.getBlockPos()), enderChest.getLevel());
    }

    public void renderChest(CCRenderState ccrs, PoseStack pose, MultiBufferSource source, int rotation, Frequency freq, float lidAngle, int pearlOffset, net.minecraft.world.level.Level level) {
        Matrix4 mat = new Matrix4(pose);
        if (lidAngle != 0) {
            renderEndPortal.render(mat, source);
        }
        ccrs.reset();

        // Render chest
        pose.pushPose();
        pose.translate(0, 1.0, 1.0);
        pose.scale(1.0F, -1.0F, -1.0F);
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(new Quaternionf().rotateXYZ(0, (float) (rotation * 90F * MathHelper.torad), 0));
        pose.translate(-0.5, -0.5, -0.5);
        VertexConsumer chestCons = source.getBuffer(chestType);
        lid.xRot = lidAngle;
        lock.xRot = lidAngle;
        diamondLock.xRot = lidAngle;
        lid.render(pose, chestCons, ccrs.brightness, ccrs.overlay);
        bottom.render(pose, chestCons, ccrs.brightness, ccrs.overlay);
        if (freq.hasOwner()) {
            diamondLock.render(pose, chestCons, ccrs.brightness, ccrs.overlay);
        } else {
            lock.render(pose, chestCons, ccrs.brightness, ccrs.overlay);
        }
        pose.popPose();

        mat.translate(0.5, 0, 0.5);
        // 顶部槽位：改为物品可视化显示（不再使用颜色按钮纹理）。
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        // Use PoseStack for item rendering with similar transforms.
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(new Quaternionf().rotateXYZ(0, (float) (-rotation * 90F * MathHelper.torad), 0));
        // rotate around chest lid pivot
        pose.translate(-8F / 16F, 9F / 16F, -7F / 16F);
        pose.mulPose(new Quaternionf().rotateXYZ(lidAngle, 0, 0));
        pose.translate(8F / 16F, -9F / 16F, 7F / 16F);

        for (int i = 0; i < 3; i++) {
            // 三角布局：0=左上，1=右上，2=中下
            float y = 14F / 16F + 0.001F;
            float x;
            float z;
            if (i == 0) { // 左上
                x = -3F / 16F;
                z = -2F / 16F;
            } else if (i == 1) { // 右上
                x = 3F / 16F;
                z = -2F / 16F;
            } else { // 中下
                x = 0F;
                z = 2F / 16F;
            }
            pose.pushPose();
            pose.translate(x, y, z);
            // Lay item flat on lid
            pose.mulPose(new Quaternionf().rotateXYZ((float) (-90F * MathHelper.torad), 0, 0));
            pose.scale(0.5F, 0.5F, 0.5F);
            switch (i) {
                case 0 -> {
                    if (!freq.getLeftStack().isEmpty()) {
                        itemRenderer.renderStatic(freq.getLeftStack(), ItemDisplayContext.FIXED, ccrs.brightness, ccrs.overlay, pose, source, level, 0);
                    }
                }
                case 1 -> {
                    if (!freq.getMiddleStack().isEmpty()) {
                        itemRenderer.renderStatic(freq.getMiddleStack(), ItemDisplayContext.FIXED, ccrs.brightness, ccrs.overlay, pose, source, level, 0);
                    }
                }
                case 2 -> {
                    if (!freq.getRightStack().isEmpty()) {
                        itemRenderer.renderStatic(freq.getRightStack(), ItemDisplayContext.FIXED, ccrs.brightness, ccrs.overlay, pose, source, level, 0);
                    }
                }
            }
            pose.popPose();
        }
        pose.popPose();

        // Pearl
        if (lidAngle != 0) {
            double time = ClientUtils.getRenderTime() + pearlOffset;
            Matrix4 pearlMat = RenderUtils.getMatrix(mat.copy(), new Vector3(0, 0.2 + lidAngle * -0.5 + RenderUtils.getPearlBob(time), 0), new Rotation(time / 3, new Vector3(0, 1, 0)), 0.04);
            ccrs.brightness = 15728880;
            ccrs.bind(pearlType, source);
            CCModelLibrary.icosahedron4.render(ccrs, pearlMat);
        }
        ccrs.reset();
    }
}
