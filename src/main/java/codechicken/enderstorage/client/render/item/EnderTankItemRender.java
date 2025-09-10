package codechicken.enderstorage.client.render.item;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.client.render.tile.RenderTileEnderTank;
import codechicken.enderstorage.network.TankSynchroniser;
import codechicken.lib.math.MathHelper;
import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Matrix4;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 末影储罐物品渲染器
 * <p>
 * 负责渲染末影储罐物品的3D模型，当末影储罐作为物品显示时
 * （如在物品栏中、掉落在地上、或在展示框中）使用此渲染器。
 * <p>
 * 该渲染器不仅渲染储罐的外观，还会显示储罐内部的液体内容，
 * 通过网络同步器获取对应频率储罐的实际液体状态。
 * <p>
 * 渲染特性：
 * - 读取物品堆叠中的频率数据并正确显示颜色
 * - 通过网络同步获取并显示储罐内的实际液体
 * - 使用与方块实体相同的渲染逻辑确保一致性
 * - 禁用环境光遮蔽以获得清晰的物品显示
 * - 配置为2D GUI显示模式
 * 
 * @author covers1624 创建于 2016年4月27日
 */
public class EnderTankItemRender implements IItemRenderer {

    /**
     * 渲染末影储罐物品
     * <p>
     * 此方法在需要渲染末影储罐物品时被调用，包括：
     * - 在玩家物品栏中显示
     * - 作为掉落物品在世界中显示
     * - 在物品展示框或其他容器中显示
     * <p>
     * 渲染过程会读取物品的频率数据（颜色信息），
     * 获取对应频率储罐中的液体，并同时渲染储罐模型和液体内容。
     * 
     * @param stack 要渲染的物品堆叠
     * @param context 显示上下文（手持、GUI、地面等）
     * @param poseStack 姿态栈，用于变换操作
     * @param source 多缓冲区源，用于渲染
     * @param packedLight 打包的光照值
     * @param packedOverlay 打包的覆盖层值
     */
    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        // 初始化渲染状态
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        
        // 从物品堆叠中读取频率信息（颜色配置）
        Frequency freq = Frequency.readFromStack(stack);
        
        // 通过网络同步器获取该频率储罐中的液体
        FluidStack fluid = TankSynchroniser.getClientLiquid(freq);
        
        // 创建变换矩阵
        Matrix4 mat = new Matrix4(poseStack);
        
        // 渲染储罐模型
        // 参数：渲染状态, 变换矩阵, 缓冲区源, 动画状态(2), 旋转角度(90度), 频率, 开启进度(0)
        RenderTileEnderTank.renderTank(ccrs, mat, source, 2, (float) (MathHelper.torad * 90F), freq, 0);
        
        // 调整位置并渲染液体
        mat.translate(-0.5, 0, -0.5);
        RenderTileEnderTank.renderFluid(ccrs, mat, source, fluid);
    }

    /**
     * 获取模型状态
     * <p>
     * 返回默认的方块变换状态，用于控制物品在不同显示上下文中的变换。
     * 
     * @return 透视模型状态
     */
    @Override
    public PerspectiveModelState getModelState() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    /**
     * 是否使用环境光遮蔽
     * <p>
     * 返回false以禁用环境光遮蔽，确保物品在物品栏中有清晰的显示效果。
     * 
     * @return false，禁用环境光遮蔽
     */
    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    /**
     * 是否为3D GUI显示
     * <p>
     * 返回false表示在GUI中以2D方式显示，而不是3D模型。
     * 
     * @return false，使用2D GUI显示
     */
    @Override
    public boolean isGui3d() {
        return false;
    }

    /**
     * 是否使用方块光照
     * <p>
     * 返回false表示不使用方块光照系统，而是使用物品光照。
     * 
     * @return false，不使用方块光照
     */
    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
