package codechicken.enderstorage.client.render.tile;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.block.BlockEnderTank;
import codechicken.enderstorage.client.model.ButtonModelLibrary;
import codechicken.enderstorage.client.render.RenderCustomEndPortal;
import codechicken.enderstorage.tile.TileEnderTank;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.fluid.FluidUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.*;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.*;
import codechicken.lib.vec.uv.UVTranslation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Map;

import static codechicken.enderstorage.EnderStorage.MOD_ID;

/**
 * 末影储罐方块实体渲染器
 * <p>
 * 负责渲染末影储罐方块实体的所有视觉组件，包括：
 * - 储罐主体模型和阀门
 * - 三个颜色按钮（频率选择器）
 * - 内部星空效果（末地门户效果）
 * - 液体内容渲染
 * - 悬浮的末影珍珠装饰
 * <p>
 * 该渲染器使用OBJ模型文件加载3D模型，并通过多种渲染技术
 * 创造丰富的视觉效果。支持动画效果包括阀门旋转、
 * 珍珠浮动和星空背景。
 * <p>
 * 渲染流程：
 * 1. 渲染星空背景（末地门户效果）
 * 2. 渲染储罐主体和动画阀门
 * 3. 渲染三个频率颜色按钮
 * 4. 渲染悬浮末影珍珠
 * 5. 渲染液体内容
 */
public class RenderTileEnderTank implements BlockEntityRenderer<TileEnderTank> {

    /** 储罐基础材质渲染类型 */
    private static final RenderType baseType = RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/endertank.png"));
    /** 按钮材质渲染类型 */
    private static final RenderType buttonType = RenderType.entitySolid(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/buttons.png"));
    /** 末影珍珠材质渲染类型 */
    private static final RenderType pearlType = CCModelLibrary.getIcos4RenderType(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/hedronmap.png"));

    /** 储罐主体3D模型 */
    public static final CCModel tankModel;
    /** 阀门3D模型 */
    public static final CCModel valveModel;
    /** 三个按钮的3D模型数组 */
    public static final CCModel[] buttons;
    /** 自定义末地门户渲染器，用于星空背景效果 */
    public static final RenderCustomEndPortal renderEndPortal = new RenderCustomEndPortal(0.1205, 0.24, 0.76, 0.24, 0.76);

    // 静态初始化块：加载和处理3D模型
    static {
        // 使用OBJ解析器加载储罐模型文件
        Map<String, CCModel> models = new OBJParser(ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/endertank.obj"))
                .quads()     // 转换为四边形网格
                .swapYZ()    // 交换Y和Z轴以适应Minecraft坐标系
                .parse();
        
        // 位置修正变换，调整模型在方块中的位置
        Transformation fix = new Translation(-0.0099 - 0.5, 0, -0.0027 - 0.5);
        
        // 提取并处理阀门模型
        valveModel = models.remove("Valve").apply(fix).computeNormals();
        
        // 组合剩余模型作为储罐主体，收缩UV以避免纹理问题
        tankModel = CCModel.combine(models.values()).apply(fix).computeNormals().shrinkUVs(0.004);

        // 创建三个按钮模型
        buttons = new CCModel[3];
        for (int i = 0; i < 3; i++) {
            // 复制按钮模型并应用对应的变换（位置和旋转）
            buttons[i] = ButtonModelLibrary.button.copy().apply(BlockEnderTank.buttonT[i].with(new Translation(-0.5, 0, -0.5)));
        }
    }

    /**
     * 构造末影储罐渲染器
     * 
     * @param context 方块实体渲染器提供者上下文
     */
    public RenderTileEnderTank(BlockEntityRendererProvider.Context context) {
    }

    /**
     * 渲染末影储罐方块实体
     * <p>
     * 这是主要的渲染入口方法，负责协调所有渲染组件。
     * 根据储罐的状态（旋转、压力、液体等）进行相应的渲染。
     * 
     * @param enderTank 要渲染的末影储罐方块实体
     * @param partialTicks 部分刻度，用于动画插值
     * @param mStack 姿态栈，用于变换操作
     * @param source 多缓冲区源，用于渲染
     * @param packedLight 打包的光照值
     * @param packedOverlay 打包的覆盖层值
     */
    @Override
    public void render(TileEnderTank enderTank, float partialTicks, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        // 初始化渲染状态
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        
        // 计算阀门旋转角度（基于压力状态插值）
        float valveRot = (float) MathHelper.interpolate(enderTank.pressure_state.b_rotate, enderTank.pressure_state.a_rotate, partialTicks) * 0.01745F;
        
        // 获取末影珍珠的时间偏移（用于动画同步）
        int pearlOffset = RenderUtils.getTimeOffset(enderTank.getBlockPos());
        
        // 创建变换矩阵
        Matrix4 mat = new Matrix4(mStack);
        
        // 渲染储罐（不包括液体）
        renderTank(ccrs, mat.copy(), source, enderTank.rotation, valveRot, enderTank.getFrequency(), pearlOffset);
        
        // 渲染液体内容
        renderFluid(ccrs, mat, source, enderTank.liquid_state.c_liquid);
        
        // 重置渲染状态
        ccrs.reset();
    }

    /**
     * 渲染储罐本体（不包括液体）
     * <p>
     * 这个静态方法也被物品渲染器使用，确保物品和方块的视觉一致性。
     * 渲染包括星空背景、储罐模型、阀门、按钮和末影珍珠。
     * 
     * @param ccrs 渲染状态
     * @param mat 变换矩阵
     * @param buffers 多缓冲区源
     * @param rotation 储罐旋转状态（0-3）
     * @param valveRot 阀门旋转角度
     * @param freq 频率信息（颜色配置）
     * @param pearlOffset 珍珠动画时间偏移
     */
    public static void renderTank(CCRenderState ccrs, Matrix4 mat, MultiBufferSource buffers, int rotation, float valveRot, Frequency freq, int pearlOffset) {
        // 渲染星空背景效果
        renderEndPortal.render(mat, buffers);
        ccrs.reset();
        
        // 调整到方块中心并应用旋转
        mat.translate(0.5, 0, 0.5);
        mat.rotate((-90 * (rotation + 2)) * MathHelper.torad, Vector3.Y_POS);
        
        // 渲染储罐主体
        ccrs.bind(baseType, buffers);
        tankModel.render(ccrs, mat);
        
        // 渲染阀门（带旋转动画）
        Matrix4 valveMat = mat.copy().apply(new Rotation(valveRot, Vector3.Z_POS).at(new Vector3(0, 0.4165, 0)));
        // 根据是否有所有者调整阀门纹理UV偏移
        valveModel.render(ccrs, valveMat, new UVTranslation(0, freq.hasOwner() ? 13 / 64D : 0));

        // 渲染三个颜色按钮
        ccrs.bind(buttonType, buffers);
        EnumColour[] colours = freq.toArray();
        for (int i = 0; i < 3; i++) {
            // 根据颜色索引计算UV偏移（4x4纹理网格）
            //noinspection IntegerDivisionInFloatingPointContext
            buttons[i].render(ccrs, mat, new UVTranslation(0.25 * (colours[i].getWoolMeta() % 4), 0.25 * (colours[i].getWoolMeta() / 4)));
        }

        // 渲染悬浮的末影珍珠
        double time = ClientUtils.getRenderTime() + pearlOffset;
        // 创建珍珠变换矩阵：位置、浮动动画、旋转动画、缩放
        Matrix4 pearlMat = RenderUtils.getMatrix(mat.copy(), new Vector3(0, 0.45 + RenderUtils.getPearlBob(time) * 2, 0), new Rotation(time / 3, Vector3.Y_POS), 0.04);
        ccrs.brightness = 15728880; // 设置珍珠的高亮度
        ccrs.bind(pearlType, buffers);
        CCModelLibrary.icosahedron4.render(ccrs, pearlMat); // 渲染二十面体珍珠
        ccrs.reset();
    }

    /**
     * 渲染储罐内的液体
     * <p>
     * 根据液体类型和数量渲染储罐内部的液体效果。
     * 液体高度根据储罐中的液体量动态调整。
     * 
     * @param ccrs 渲染状态
     * @param mat 变换矩阵
     * @param getter 多缓冲区源
     * @param stack 液体堆叠（包含类型和数量）
     */
    public static void renderFluid(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter, FluidStack stack) {
        // 定义液体渲染区域：从储罐底部到顶部，根据液体量调整高度
        // 参数：渲染状态, 变换矩阵, 渲染类型, 缓冲区源, 液体堆叠, 渲染立方体区域, 填充比例, 透明度
        RenderUtils.renderFluidCuboid(ccrs, mat, RenderUtils.getFluidRenderType(), getter, stack, 
            new Cuboid6(0.22, 0.12, 0.22, 0.78, 0.121 + 0.63, 0.78), // 液体渲染区域
            stack.getAmount() / (16D * FluidUtils.B), // 填充比例（基于液体数量）
            0.75); // 透明度
    }
}
