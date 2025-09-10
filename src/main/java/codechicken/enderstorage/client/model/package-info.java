/**
 * 客户端模型包 - 3D模型和模型组件管理
 * 
 * <p>
 * 本包负责管理所有与 EnderStorage 模组相关的 3D 模型资源，包括方块模型、
 * 物品模型、动态模型组件以及模型生成和管理工具。
 * </p>
 * 
 * <h2>核心模型类</h2>
 * 
 * <h3>主要模型组件</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.client.model.ButtonModelLibrary} - 按钮模型库管理器</li>
 * </ul>
 * 
 * <h2>模型系统</h2>
 * 
 * <h3>按钮模型系统</h3>
 * <ul>
 *   <li><strong>颜色按钮</strong> - 16种不同颜色的频率按钮模型</li>
 *   <li><strong>动态生成</strong> - 根据颜色动态生成按钮模型</li>
 *   <li><strong>状态管理</strong> - 管理按钮的按下/弹起状态</li>
 *   <li><strong>动画支持</strong> - 支持按钮点击动画效果</li>
 * </ul>
 * 
 * <h3>模型生成</h3>
 * <p>
 * 动态生成不同颜色和状态的模型组件：
 * </p>
 * <pre>{@code
 * // 生成指定颜色的按钮模型
 * public BakedModel generateButtonModel(EnumDyeColor color, boolean pressed) {
 *     ModelBuilder builder = new ModelBuilder();
 *     
 *     // 设置按钮颜色
 *     int colorValue = color.getFireworkColor();
 *     builder.setTexture("button", getButtonTexture(color));
 *     
 *     // 设置按钮位置（按下或弹起）
 *     float offset = pressed ? 0.8f : 1.0f;
 *     builder.setTransform(new Vector3f(0, offset, 0));
 *     
 *     return builder.build();
 * }
 * }</pre>
 * 
 * <h2>模型组件</h2>
 * 
 * <h3>方块模型组件</h3>
 * <ul>
 *   <li><strong>箱体模型</strong> - 末影箱子的主体结构模型</li>
 *   <li><strong>盖子模型</strong> - 可开合的箱子盖模型</li>
 *   <li><strong>频率按钮</strong> - 三个颜色频率调节按钮</li>
 *   <li><strong>锁按钮</strong> - 私人/公共模式切换按钮</li>
 * </ul>
 * 
 * <h3>储罐模型组件</h3>
 * <ul>
 *   <li><strong>罐体模型</strong> - 末影储罐的主体结构</li>
 *   <li><strong>流体渲染</strong> - 储罐内部流体的模型</li>
 *   <li><strong>液位指示</strong> - 显示当前流体液位</li>
 * </ul>
 * 
 * <h2>模型加载系统</h2>
 * 
 * <h3>资源加载</h3>
 * <p>
 * 模型从资源包中加载并缓存：
 * </p>
 * <pre>{@code
 * // 模型加载和缓存
 * @SubscribeEvent
 * public static void onModelBake(ModelEvent.BakingCompleted event) {
 *     ModelManager modelManager = event.getModelManager();
 *     
 *     // 加载按钮模型资源
 *     for (EnumDyeColor color : EnumDyeColor.values()) {
 *         ResourceLocation location = new ResourceLocation(
 *             "enderstorage", "block/button_" + color.getName());
 *         BakedModel model = modelManager.getModel(location);
 *         ButtonModelLibrary.registerButtonModel(color, model);
 *     }
 * }
 * }</pre>
 * 
 * <h3>模型缓存</h3>
 * <ul>
 *   <li><strong>静态缓存</strong> - 缓存预制的静态模型</li>
 *   <li><strong>动态缓存</strong> - 缓存运行时生成的模型</li>
 *   <li><strong>LRU 策略</strong> - 使用最近最少使用策略管理缓存</li>
 * </ul>
 * 
 * <h2>动画系统</h2>
 * 
 * <h3>模型动画</h3>
 * <p>
 * 支持多种模型动画效果：
 * </p>
 * <ul>
 *   <li><strong>箱子开合</strong> - 箱子盖的开合动画</li>
 *   <li><strong>按钮点击</strong> - 频率按钮的点击反馈</li>
 *   <li><strong>颜色变化</strong> - 频率改变时的颜色渐变</li>
 *   <li><strong>流体波动</strong> - 储罐中流体的波动效果</li>
 * </ul>
 * 
 * <h2>性能优化</h2>
 * 
 * <h3>模型优化策略</h3>
 * <p>
 * 实现了多种性能优化措施：
 * </p>
 * <ul>
 *   <li><strong>模型合并</strong> - 将多个组件合并成一个模型</li>
 *   <li><strong>红吉箱剪裁</strong> - 只渲染可见的模型部分</li>
 *   <li><strong>批量渲染</strong> - 将相同材质的模型批量渲染</li>
 * </ul>
 * 
 * <h2>兼容性</h2>
 * 
 * <h3>模型格式支持</h3>
 * <ul>
 *   <li><strong>JSON 模型</strong> - 支持 Minecraft 原生 JSON 模型</li>
 *   <li><strong>OBJ 模型</strong> - 支持外部 OBJ 模型导入</li>
 *   <li><strong>自定义格式</strong> - 支持模组的自定义模型格式</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.client.render
 * @see codechicken.enderstorage.client
 */
@NonNullApi
package codechicken.enderstorage.client.model;

import net.covers1624.quack.annotation.NonNullApi;
