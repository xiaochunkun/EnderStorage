package codechicken.enderstorage.client.render.entity;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.client.render.tile.RenderTileEnderTank;
import codechicken.lib.fluid.FluidUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 储罐层渲染器
 * <p>
 * 这是一个特殊的玩家渲染层，为特定的开发者和贡献者在游戏中添加
 * 末影储罐装饰效果。该效果会在玩家头部渲染一个迷你版的末影储罐，
 * 包含动态的液体效果，作为对模组开发团队的特殊标识。
 * <p>
 * 功能特性：
 * - 仅对特定UUID列表中的玩家生效
 * - 储罐会跟随玩家头部运动
 * - 支持玩家蹲下和滑翔状态的适配
 * - 包含动态的液体上下浮动效果
 * - 使用水作为默认液体显示
 * <p>
 * 这是一个"彩蛋"功能，用于表彰模组的开发者和重要贡献者。
 * 
 * @author covers1624 创建于 2016年12月15日
 */
public class TankLayerRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    /** 
     * 享有储罐装饰效果的特殊玩家UUID字符串数组
     * <p>
     * 这些UUID通常属于模组的开发者、重要贡献者或其他特殊人员。
     * 只有这些玩家在游戏中才会显示储罐装饰效果。
     */
    private static final String[] UUID_STRINGS = {
            "c85f3fd3-1754-45ec-ab3d-a33d6312dfef",  // 特殊玩家UUID 1
            "c501d550-7e3c-463e-8a95-256f86d9a47d",  // 特殊玩家UUID 2
            "cf3e2c7e-d703-48e0-808e-f139bf26ff9d",  // 特殊玩家UUID 3
            "44ba40ef-fd8a-446f-834b-5aea42119c92"   // 特殊玩家UUID 4
    };
    
    /** 将UUID字符串转换为UUID对象的集合，便于快速查找 */
    private static final Set<UUID> UUIDS = Arrays.stream(UUID_STRINGS)
            .map(UUID::fromString)
            .collect(Collectors.toSet());
            
    /** 空白频率对象，用于渲染没有颜色设置的储罐 */
    private static final Frequency BLANK = new Frequency();

    /**
     * 构造储罐层渲染器
     * 
     * @param parent 父渲染器，通常是玩家模型渲染器
     */
    public TankLayerRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    /**
     * 渲染储罐装饰层
     * <p>
     * 此方法在每帧都会被调用，用于渲染玩家头部的储罐装饰。
     * 只有UUID在特殊列表中的玩家才会显示这个效果。
     * <p>
     * 渲染过程：
     * 1. 检查玩家UUID是否在特殊列表中
     * 2. 设置渲染状态和变换矩阵
     * 3. 根据玩家姿态调整储罐位置
     * 4. 渲染储罐模型和动态液体
     * 
     * @param mStack 姿态栈，用于变换操作
     * @param buffers 多缓冲区源，用于渲染
     * @param packedLight 光照值
     * @param entity 要渲染的玩家实体
     * @param limbSwing 肢体摆动角度
     * @param limbSwingAmount 肢体摆动幅度
     * @param partialTicks 部分刻度，用于插值
     * @param ageInTicks 实体存在时间
     * @param netHeadYaw 头部偏航角
     * @param headPitch 头部俯仰角
     */
    @Override
    public void render(PoseStack mStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // 只为特殊UUID列表中的玩家渲染储罐装饰
        if (UUIDS.contains(entity.getUUID())) {
            // 设置渲染状态
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.brightness = packedLight;
            ccrs.overlay = OverlayTexture.NO_OVERLAY;
            
            // 创建变换矩阵并设置基础变换
            Matrix4 mat = new Matrix4(mStack);
            mat.rotate(MathHelper.torad * 180, Vector3.X_POS);  // 180度X轴旋转
            mat.scale(0.5);  // 缩放到一半大小
            
            // 根据玩家状态调整储罐位置
            if (entity.isCrouching()) {
                mat.translate(0, -0.5, 0);  // 蹲下时向下移动
            }
            if (entity.isFallFlying()) {
                headPitch = -45;  // 滑翔时固定俯仰角
            }
            
            // 应用头部旋转
            mat.rotate(netHeadYaw * MathHelper.torad, Vector3.Y_NEG);   // 偏航角旋转
            mat.rotate(headPitch * MathHelper.torad, Vector3.X_POS);    // 俯仰角旋转
            mat.translate(-0.5, 1, -0.5);  // 调整到头部位置
            
            // 渲染储罐模型（无颜色，90度旋转）
            RenderTileEnderTank.renderTank(ccrs, mat, buffers, 0, (float) (MathHelper.torad * 90F), BLANK, 0);

            // 创建动态液体效果
            FluidStack stack = FluidUtils.water.copy();  // 使用水作为液体
            
            // 计算液体的动态浮动效果
            float bob = 0.45F + RenderUtils.getPearlBob(ClientUtils.getRenderTime()) * 2;
            // 将浮动值映射到液体容量（1000-14000mB）
            stack.setAmount((int) MathHelper.map(bob, 0.2, 0.6, 1000, 14000));
            
            // 调整液体渲染位置并渲染
            mat.translate(-0.5, 0, -0.5);
            RenderTileEnderTank.renderFluid(ccrs, mat, buffers, stack);
        }
    }
}
