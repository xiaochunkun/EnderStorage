/**
 * 存储插件包 - 可扩展的存储类型插件系统
 * 
 * <p>
 * 本包实现了 EnderStorage 模组的插件化存储系统，允许扩展和自定义
 * 不同类型的存储实现，并提供统一的接口和管理机制。
 * </p>
 * 
 * <h2>核心插件类</h2>
 * 
 * <h3>存储插件实现</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.plugin.EnderItemStoragePlugin} - 物品存储插件</li>
 *   <li>{@link codechicken.enderstorage.plugin.EnderLiquidStoragePlugin} - 流体存储插件</li>
 * </ul>
 * 
 * <h2>插件架构</h2>
 * 
 * <h3>物品存储插件</h3>
 * <ul>
 *   <li><strong>27槽位存储</strong> - 提供27个物品槽位</li>
 *   <li><strong>物品处理</strong> - 实现IItemHandler接口</li>
 *   <li><strong>自动化支持</strong> - 支持各种自动化模组</li>
 * </ul>
 * 
 * <h3>流体存储插件</h3>
 * <ul>
 *   <li><strong>大容量存储</strong> - 支持大量流体存储</li>
 *   <li><strong>流体处理</strong> - 实现IFluidHandler接口</li>
 *   <li><strong>管道兼容</strong> - 与各种流体管道兼容</li>
 * </ul>
 * 
 * <h2>插件功能</h2>
 * 
 * <h3>存储管理</h3>
 * <ul>
 *   <li><strong>动态创建</strong> - 按需创建存储实例</li>
 *   <li><strong>数据持久化</strong> - 自动保存和加载数据</li>
 *   <li><strong>网络同步</strong> - 客户端和服务器端同步</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.api
 * @see codechicken.enderstorage.storage
 */
@NonNullApi
package codechicken.enderstorage.plugin;

import net.covers1624.quack.annotation.NonNullApi;
