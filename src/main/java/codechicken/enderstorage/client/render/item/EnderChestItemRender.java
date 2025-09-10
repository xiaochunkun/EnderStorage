package codechicken.enderstorage.client.render.item;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.client.render.tile.RenderTileEnderChest;
import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 末影箱物品渲染器
 * <p>
 * 负责渲染末影箱物品的3D模型，当末影箱作为物品显示时
 * （如在物品栏中、掉落在地上、或在展示框中）使用此渲染器。
 * <p>
 * 该渲染器通过调用方块实体渲染器来实现物品的3D渲染，
 * 确保物品形态与放置后的方块形态保持一致。
 * <p>
 * 渲染特性：
 * - 读取物品堆叠中的频率数据并正确显示颜色
 * - 使用与方块实体相同的渲染逻辑确保一致性
 * - 禁用环境光遮蔽以获得清晰的物品显示
 * - 配置为2D GUI显示模式
 * 
 * @author covers1624 创建于 2016年4月27日
 */
public class EnderChestItemRender implements IItemRenderer {

    /** 方块实体渲染器实例，用于复用渲染逻辑 */
    private final RenderTileEnderChest tileRender = new RenderTileEnderChest(null);

    /**
     * 渲染末影箱物品
     * <p>
     * 此方法在需要渲染末影箱物品时被调用，包括：
     * - 在玩家物品栏中显示
     * - 作为掉落物品在世界中显示
     * - 在物品展示框或其他容器中显示
     * <p>
     * 渲染过程会读取物品的频率数据（颜色信息），
     * 并使用相应的颜色渲染箱子模型。
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
        
        // 从物品堆叠中读取频率信息（颜色配置）
        Frequency freq = Frequency.readFromStack(stack);
        
        // 设置光照和覆盖层
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        
        // 使用方块实体渲染器渲染箱子
        // 参数：渲染状态, 姿态栈, 缓冲区源, 动画状态(2), 频率, 旋转角度(0), 开启进度(0)
        tileRender.renderChest(ccrs, poseStack, source, 2, freq, 0, 0);
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
