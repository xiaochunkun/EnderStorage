package codechicken.enderstorage.client.render.tile;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.block.BlockEnderTank;
import codechicken.enderstorage.client.render.RenderCustomEndPortal;
import codechicken.enderstorage.tile.TileEnderTank;
import codechicken.lib.fluid.FluidUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.*;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

import java.util.Map;

public class RenderTileEnderTank implements BlockEntityRenderer<TileEnderTank> {

    private static final RenderType baseType = RenderType.entityCutout(new ResourceLocation("enderstorage:textures/endertank.png"));
    private static final RenderType buttonType = RenderType.entitySolid(new ResourceLocation("enderstorage:textures/buttons.png"));
    private static final RenderType pearlType = CCModelLibrary.getIcos4RenderType(new ResourceLocation("enderstorage:textures/hedronmap.png"));

    public static final CCModel tankModel;
    public static final CCModel valveModel;
    // public static final CCModel[] buttons;
    public static final RenderCustomEndPortal renderEndPortal = new RenderCustomEndPortal(0.1205, 0.24, 0.76, 0.24, 0.76);

    static {
        Map<String, CCModel> models = new OBJParser(new ResourceLocation("enderstorage:models/endertank.obj"))
                .quads()
                .swapYZ()
                .parse();
        Transformation fix = new Translation(-0.0099 - 0.5, 0, -0.0027 - 0.5);
        valveModel = models.remove("Valve").apply(fix).computeNormals();
        tankModel = CCModel.combine(models.values()).apply(fix).computeNormals().shrinkUVs(0.004);

        // buttons removed; item rendering is used instead.
    }

    public RenderTileEnderTank(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEnderTank enderTank, float partialTicks, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        float valveRot = (float) MathHelper.interpolate(enderTank.pressure_state.b_rotate, enderTank.pressure_state.a_rotate, partialTicks) * 0.01745F;
        int pearlOffset = RenderUtils.getTimeOffset(enderTank.getBlockPos());
        Matrix4 mat = new Matrix4(mStack);
        renderTank(ccrs, mat.copy(), mStack, source, enderTank.rotation, valveRot, enderTank.getFrequency(), pearlOffset, enderTank.getLevel(), enderTank.getBlockPos());
        renderFluid(ccrs, mat, source, enderTank.liquid_state.c_liquid);
        ccrs.reset();
    }

    public static void renderTank(CCRenderState ccrs, Matrix4 mat, PoseStack pose, MultiBufferSource buffers, int rotation, float valveRot, Frequency freq, int pearlOffset, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        renderEndPortal.render(mat, buffers);
        ccrs.reset();
        mat.translate(0.5, 0, 0.5);
        mat.rotate((-90 * (rotation + 2)) * MathHelper.torad, Vector3.Y_POS);
        ccrs.bind(baseType, buffers);
        tankModel.render(ccrs, mat);
        Matrix4 valveMat = mat.copy().apply(new Rotation(valveRot, Vector3.Z_POS).at(new Vector3(0, 0.4165, 0)));
        valveModel.render(ccrs, valveMat);

        // 顶部槽位：以物品显示，不再使用按钮纹理
        pose.pushPose();
        // Align with 'mat' transforms: mat currently has translated(0.5,0,0.5) and rotated around Y
        // Recreate similar transform for ItemRenderer
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(new Quaternionf().rotateXYZ(0, (float) ((-90 * (rotation + 2)) * MathHelper.torad), 0));
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (int i = 0; i < 3; i++) {
            // 三角布局：0=左上，1=右上，2=中下（绝对坐标，随后减去 0.5 进入局部）
            double y = 0.91 + 0.001;
            double x;
            double z;
            if (i == 0) { // 左上
                x = 0.40; z = 0.42;
            } else if (i == 1) { // 右上
                x = 0.60; z = 0.42;
            } else { // 中下
                x = 0.50; z = 0.58;
            }
            pose.pushPose();
            pose.translate(x - 0.5, y, z - 0.5);
            // Lay item flat
            pose.mulPose(new Quaternionf().rotateXYZ((float) (-90F * MathHelper.torad), 0, 0));
            // 缩放为原来的 1/3
            pose.scale(0.33333334F, 0.33333334F, 0.33333334F);
            // 使用槽位所在位置的光照，修复 3D 物品光照
            int itemLight = ccrs.brightness;
            if (level != null && pos != null) {
                itemLight = net.minecraft.client.renderer.LevelRenderer.getLightColor(level, pos.above());
            }
            switch (i) {
                case 0 -> {
                    if (!freq.getLeftStack().isEmpty()) {
                        itemRenderer.renderStatic(freq.getLeftStack(), ItemDisplayContext.FIXED, itemLight, ccrs.overlay, pose, buffers, level, 0);
                    }
                }
                case 1 -> {
                    if (!freq.getMiddleStack().isEmpty()) {
                        itemRenderer.renderStatic(freq.getMiddleStack(), ItemDisplayContext.FIXED, itemLight, ccrs.overlay, pose, buffers, level, 0);
                    }
                }
                case 2 -> {
                    if (!freq.getRightStack().isEmpty()) {
                        itemRenderer.renderStatic(freq.getRightStack(), ItemDisplayContext.FIXED, itemLight, ccrs.overlay, pose, buffers, level, 0);
                    }
                }
            }
            pose.popPose();
        }
        pose.popPose();

        double time = ClientUtils.getRenderTime() + pearlOffset;
        Matrix4 pearlMat = RenderUtils.getMatrix(mat.copy(), new Vector3(0, 0.45 + RenderUtils.getPearlBob(time) * 2, 0), new Rotation(time / 3, Vector3.Y_POS), 0.04);
        ccrs.brightness = 15728880;
        ccrs.bind(pearlType, buffers);
        CCModelLibrary.icosahedron4.render(ccrs, pearlMat);
        ccrs.reset();
    }

    public static void renderFluid(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter, FluidStack stack) {
        RenderUtils.renderFluidCuboid(ccrs, mat, RenderUtils.getFluidRenderType(), getter, stack, new Cuboid6(0.22, 0.12, 0.22, 0.78, 0.121 + 0.63, 0.78), stack.getAmount() / (16D * FluidUtils.B), 0.75);
    }
}
