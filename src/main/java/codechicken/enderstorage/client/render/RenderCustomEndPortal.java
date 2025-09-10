package codechicken.enderstorage.client.render;

import codechicken.enderstorage.client.Shaders;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

/**
 * 自定义末地门户渲染器
 * <p>
 * 负责渲染末影储罐内部的星空效果，模拟末影维度的深邃空间感。
 * 该渲染器使用自定义的星空着色器来创建动态的星空背景，
 * 根据玩家的视角和时间变化来调整星空的显示效果。
 * <p>
 * 渲染特性：
 * - 使用专用的星空着色器实现动态星空效果
 * - 根据玩家的偏航角和俯仰角调整星空视角
 * - 支持时间动画，创造流动的星空效果
 * - 支持两种渲染模式：Matrix4变换和PoseStack变换
 * <p>
 * 该渲染器主要用于末影储罐的内部液体表面渲染，
 * 为玩家提供沉浸式的末影维度视觉体验。
 */
public class RenderCustomEndPortal {

    /** 
     * 星空渲染类型
     * <p>
     * 定义了渲染星空效果所需的所有渲染状态：
     * - 使用自定义星空着色器
     * - 使用末地门户纹理作为基础
     * - 配置适当的混合和深度测试设置
     */
    private static final RenderType STARFIELD_TYPE = RenderType.create("starfield", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(Shaders::starfieldShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false))
                    .createCompositeState(false)
    );

    /** 渲染表面的Y坐标（高度） */
    private final double surfaceY;
    /** 渲染表面的X1坐标（左边界） */
    private final double surfaceX1;
    /** 渲染表面的X2坐标（右边界） */
    private final double surfaceX2;
    /** 渲染表面的Z1坐标（前边界） */
    private final double surfaceZ1;
    /** 渲染表面的Z2坐标（后边界） */
    private final double surfaceZ2;

    /**
     * 构造自定义末地门户渲染器
     * <p>
     * 定义渲染星空效果的3D表面区域。通常这个表面代表
     * 末影储罐内部液体的顶部，玩家可以看到星空效果。
     * 
     * @param y 表面的Y坐标（高度）
     * @param x1 表面的X1坐标（左边界）
     * @param x2 表面的X2坐标（右边界）
     * @param z1 表面的Z1坐标（前边界）
     * @param z2 表面的Z2坐标（后边界）
     */
    public RenderCustomEndPortal(double y, double x1, double x2, double z1, double z2) {
        surfaceY = y;
        surfaceX1 = x1;
        surfaceX2 = x2;
        surfaceZ1 = z1;
        surfaceZ2 = z2;
    }

    /**
     * 使用Matrix4变换矩阵渲染星空效果
     * <p>
     * 此方法使用传统的Matrix4变换方式来渲染星空。
     * 主要用于兼容某些特定的渲染管线或性能优化场景。
     * 
     * @param mat 4x4变换矩阵，用于位置变换
     * @param source 多缓冲区源，用于获取渲染缓冲区
     */
    public void render(Matrix4 mat, MultiBufferSource source) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        assert localPlayer != null;

        // 设置着色器统一变量
        Shaders.starfieldTime().glUniform1f((float) ClientUtils.getRenderTime());  // 当前渲染时间
        Shaders.starfieldYaw().glUniform1f((float) (localPlayer.getYRot() * MathHelper.torad));  // 玩家偏航角（弧度）
        Shaders.starfieldPitch().glUniform1f((float) -(localPlayer.getXRot() * MathHelper.torad)); // 玩家俯仰角（弧度，取负值）

        // 创建变换顶点消费者并渲染四边形
        VertexConsumer cons = new TransformingVertexConsumer(source.getBuffer(STARFIELD_TYPE), mat);
        cons.addVertex((float) surfaceX1, (float) surfaceY, (float) surfaceZ1); // 左前角
        cons.addVertex((float) surfaceX1, (float) surfaceY, (float) surfaceZ2); // 左后角
        cons.addVertex((float) surfaceX2, (float) surfaceY, (float) surfaceZ2); // 右后角
        cons.addVertex((float) surfaceX2, (float) surfaceY, (float) surfaceZ1); // 右前角
    }

    /**
     * 使用PoseStack姿态栈渲染星空效果
     * <p>
     * 此方法使用现代的PoseStack变换方式来渲染星空。
     * 这是推荐的渲染方式，与Minecraft的现代渲染管线兼容性更好。
     * 
     * @param pStack 姿态栈，包含变换矩阵
     * @param source 多缓冲区源，用于获取渲染缓冲区
     */
    public void render(PoseStack pStack, MultiBufferSource source) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        assert localPlayer != null;

        // 设置着色器统一变量
        Shaders.starfieldTime().glUniform1f((float) ClientUtils.getRenderTime());  // 当前渲染时间
        Shaders.starfieldYaw().glUniform1f((float) (localPlayer.getYRot() * MathHelper.torad));  // 玩家偏航角（弧度）
        Shaders.starfieldPitch().glUniform1f((float) -(localPlayer.getXRot() * MathHelper.torad)); // 玩家俯仰角（弧度，取负值）

        // 获取顶点消费者并渲染四边形
        VertexConsumer cons = source.getBuffer(STARFIELD_TYPE);
        cons.addVertex(pStack.last().pose(), (float) surfaceX1, (float) surfaceY, (float) surfaceZ1); // 左前角
        cons.addVertex(pStack.last().pose(), (float) surfaceX1, (float) surfaceY, (float) surfaceZ2); // 左后角
        cons.addVertex(pStack.last().pose(), (float) surfaceX2, (float) surfaceY, (float) surfaceZ2); // 右后角
        cons.addVertex(pStack.last().pose(), (float) surfaceX2, (float) surfaceY, (float) surfaceZ1); // 右前角
    }
}
