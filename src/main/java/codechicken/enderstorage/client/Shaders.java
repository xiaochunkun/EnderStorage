package codechicken.enderstorage.client;

import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.render.shader.CCUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import static codechicken.enderstorage.EnderStorage.MOD_ID;
import static java.util.Objects.requireNonNull;

/**
 * 着色器管理类
 * <p>
 * 负责管理和初始化末影存储模组使用的所有着色器程序。
 * 主要包含星空着色器，用于渲染末影储罐内部的星空效果，
 * 创造沉浸式的末影维度视觉体验。
 * <p>
 * 着色器系统包含以下组件：
 * - 星空着色器：渲染动态星空背景
 * - 着色器统一变量：控制星空的时间、视角和透明度
 * <p>
 * 该类在客户端初始化时自动注册着色器，并提供静态方法
 * 供其他渲染器使用这些着色器资源。
 * 
 * @author covers1624 创建于 2022年6月4日
 */
public class Shaders {

    /** 防止重复初始化的崩溃锁 */
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    /** 星空着色器实例，用于渲染末影储罐内部的星空效果 */
    private static @Nullable CCShaderInstance starfieldShader;
    /** 时间统一变量，控制星空动画的时间进程 */
    private static @Nullable CCUniform starfieldTime;
    /** 偏航角统一变量，控制星空在水平方向的视角 */
    private static @Nullable CCUniform starfieldYaw;
    /** 俯仰角统一变量，控制星空在垂直方向的视角 */
    private static @Nullable CCUniform starfieldPitch;
    /** 透明度统一变量，控制星空效果的可见度 */
    private static @Nullable CCUniform starfieldAlpha;

    /**
     * 着色器系统初始化方法
     * <p>
     * 注册着色器注册事件监听器，确保在客户端设置阶段
     * 正确注册所有需要的着色器程序。
     * 
     * @param modBus 模组事件总线
     */
    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(Shaders::onRegisterShaders);
    }

    /**
     * 着色器注册事件处理方法
     * <p>
     * 注册星空着色器并获取所有相关的统一变量引用。
     * 着色器文件位于 assets/enderstorage/shaders/starfield
     * <p>
     * 统一变量说明：
     * - Time: 控制星空动画的时间流逝
     * - Yaw: 控制观察者的水平视角
     * - Pitch: 控制观察者的垂直视角
     * - Alpha: 控制星空效果的整体透明度
     * 
     * @param event 着色器注册事件
     */
    private static void onRegisterShaders(RegisterShadersEvent event) {
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "starfield"), DefaultVertexFormat.POSITION), e -> {
            starfieldShader = (CCShaderInstance) e;
            starfieldTime = starfieldShader.getUniform("Time");
            starfieldYaw = starfieldShader.getUniform("Yaw");
            starfieldPitch = starfieldShader.getUniform("Pitch");
            starfieldAlpha = starfieldShader.getUniform("Alpha");
        });
    }

    // 着色器和统一变量的安全访问器方法
    // 使用 @formatter:off 注解保持格式化程序的代码布局
    
    /**
     * 获取星空着色器实例
     * @return 星空着色器实例，不为null
     * @throws NullPointerException 如果着色器未初始化
     */
    public static CCShaderInstance starfieldShader() { return requireNonNull(starfieldShader); }
    
    /**
     * 获取时间统一变量
     * @return 时间统一变量，不为null
     * @throws NullPointerException 如果统一变量未初始化
     */
    public static CCUniform starfieldTime() { return requireNonNull(starfieldTime); }
    
    /**
     * 获取偏航角统一变量
     * @return 偏航角统一变量，不为null
     * @throws NullPointerException 如果统一变量未初始化
     */
    public static CCUniform starfieldYaw() { return requireNonNull(starfieldYaw); }
    
    /**
     * 获取俯仰角统一变量
     * @return 俯仰角统一变量，不为null
     * @throws NullPointerException 如果统一变量未初始化
     */
    public static CCUniform starfieldPitch() { return requireNonNull(starfieldPitch); }
    
    /**
     * 获取透明度统一变量
     * @return 透明度统一变量，不为null
     * @throws NullPointerException 如果统一变量未初始化
     */
    public static CCUniform starfieldAlpha() { return requireNonNull(starfieldAlpha); }
    // @formatter:on
}
