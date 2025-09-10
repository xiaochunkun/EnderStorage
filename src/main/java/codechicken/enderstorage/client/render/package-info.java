/**
 * 客户端渲染包 - 末影存储设备的渲染实现
 * 
 * <p>
 * 本包是 EnderStorage 模组渲染系统的核心，负责所有末影存储设备的
 * 视觉效果渲染，包括特殊效果、动画和交互显示。
 * </p>
 * 
 * <h2>渲染类型</h2>
 * 
 * <h3>主要渲染器</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.client.render.RenderCustomEndPortal} - 自定义末地传送门渲染器</li>
 * </ul>
 * 
 * <h3>渲染子系统</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.client.render.tile} - 方块实体渲染器</li>
 *   <li>{@link codechicken.enderstorage.client.render.item} - 物品渲染器</li>
 *   <li>{@link codechicken.enderstorage.client.render.entity} - 实体渲染器</li>
 * </ul>
 * 
 * <h2>渲染特性</h2>
 * 
 * <h3>特殊效果</h3>
 * <ul>
 *   <li><strong>末地传送门</strong> - 高质量的末地传送门效果</li>
 *   <li><strong>频率显示</strong> - 动态颜色显示</li>
 *   <li><strong>流体渲染</strong> - 高级流体渲染效果</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.client
 */
@NonNullApi
package codechicken.enderstorage.client.render;

import net.covers1624.quack.annotation.NonNullApi;
