/**
 * 客户端核心包 - 客户端特定功能和资源管理
 * 
 * <p>
 * 本包包含了 EnderStorage 模组在客户端的所有核心功能，包括着色器管理、
 * 渲染资源初始化、GUI 集成以及客户端特定的工具类。
 * </p>
 * 
 * <h2>核心组件</h2>
 * 
 * <h3>主要类</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.client.Shaders} - 着色器资源管理</li>
 * </ul>
 * 
 * <h2>子包结构</h2>
 * 
 * <h3>功能模块</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.client.gui} - 用户界面实现</li>
 *   <li>{@link codechicken.enderstorage.client.model} - 3D模型和模型库</li>
 *   <li>{@link codechicken.enderstorage.client.render} - 渲染器实现</li>
 * </ul>
 * 
 * <h2>着色器系统</h2>
 * 
 * <h3>着色器资源</h3>
 * <p>
 * 管理所有渲染相关的着色器程序：
 * </p>
 * <ul>
 *   <li><strong>端传送门渲染</strong> - 特殊的端传送门视觉效果</li>
 *   <li><strong>流体渲染</strong> - 储罐中流体的高质量渲染</li>
 *   <li><strong>颜色渲染</strong> - 动态颜色效果渲染</li>
 * </ul>
 * 
 * <h3>着色器加载</h3>
 * <pre>{@code
 * // 初始化着色器资源
 * @SubscribeEvent
 * public static void onRegisterShaders(RegisterShadersEvent event) {
 *     try {
 *         Shaders.enderPortalShader = event.registerShader(
 *             new ShaderInstance(event.getResourceProvider(),
 *                 new ResourceLocation("enderstorage", "ender_portal"),
 *                 DefaultVertexFormat.POSITION)
 *         );
 *     } catch (IOException e) {
 *         throw new RuntimeException("Failed to load shader", e);
 *     }
 * }
 * }</pre>
 * 
 * <h2>资源管理</h2>
 * 
 * <h3>材质和纹理</h3>
 * <p>
 * 管理渲染所需的所有视觉资源：
 * </p>
 * <ul>
 *   <li><strong>模型材质</strong> - 方块和物品的材质定义</li>
 *   <li><strong>动态纹理</strong> - 频率颜色的动态生成</li>
 *   <li><strong>特效纹理</strong> - 粒子效果和动画纹理</li>
 * </ul>
 * 
 * <h2>性能优化</h2>
 * 
 * <h3>渲染优化</h3>
 * <p>
 * 客户端实现了多种性能优化策略：
 * </p>
 * <ul>
 *   <li><strong>批量渲染</strong> - 将相同类型的渲染调用批量处理</li>
 *   <li><strong>视锥体剪裁</strong> - 只渲染视锥体内的对象</li>
 *   <li><strong>LOD 系统</strong> - 根据距离调整渲染质量</li>
 *   <li><strong>缓存策略</strong> - 缓存渲染结果减少重复计算</li>
 * </ul>
 * 
 * <h2>事件集成</h2>
 * 
 * <h3>客户端事件</h3>
 * <p>
 * 处理各种客户端特定事件：
 * </p>
 * <ul>
 *   <li><strong>渲染事件</strong> - 特殊渲染效果处理</li>
 *   <li><strong>资源加载</strong> - 资源包加载和重载事件</li>
 *   <li><strong>用户交互</strong> - 键盘和鼠标交互事件</li>
 * </ul>
 * 
 * <h2>兼容性支持</h2>
 * 
 * <h3>渲染引擎</h3>
 * <p>
 * 支持多种渲染引擎和图形API：
 * </p>
 * <ul>
 *   <li><strong>OpenGL</strong> - 传统OpenGL渲染管线</li>
 *   <li><strong>Vulkan</strong> - 新一代低开销图形API</li>
 *   <li><strong>着色器兼容</strong> - 不同版本的着色器支持</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.client.gui
 * @see codechicken.enderstorage.client.render
 */
@NonNullApi
package codechicken.enderstorage.client;

import net.covers1624.quack.annotation.NonNullApi;
