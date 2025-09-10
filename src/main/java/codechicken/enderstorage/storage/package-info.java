/**
 * 存储实现包 - 具体的存储类型实现
 * 
 * <p>
 * 本包包含了 EnderStorage 模组的具体存储类型实现，包括物品存储和
 * 流体存储的核心功能，为不同类型的存储操作提供统一的接口。
 * </p>
 * 
 * <h2>核心存储类</h2>
 * 
 * <h3>主要存储实现</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.storage.EnderItemStorage} - 物品存储实现</li>
 *   <li>{@link codechicken.enderstorage.storage.EnderLiquidStorage} - 流体存储实现</li>
 * </ul>
 * 
 * <h2>存储特性</h2>
 * 
 * <h3>物品存储</h3>
 * <ul>
 *   <li><strong>27槽位容量</strong> - 提供标准的箱子存储空间</li>
 *   <li><strong>IItemHandler接口</strong> - 实现Forge标准物品处理接口</li>
 *   <li><strong>自动化兼容</strong> - 支持各种物品传输系统</li>
 *   <li><strong>智能分类</strong> - 支持物品分类和整理</li>
 * </ul>
 * 
 * <h3>流体存储</h3>
 * <ul>
 *   <li><strong>大容量存储</strong> - 支持大量流体存储</li>
 *   <li><strong>IFluidHandler接口</strong> - 实现Forge标准流体处理接口</li>
 *   <li><strong>流体混合</strong> - 支持不同流体的混合存储</li>
 *   <li><strong>温度管理</strong> - 管理流体的温度属性</li>
 * </ul>
 * 
 * <h2>数据管理</h2>
 * 
 * <h3>持久化支持</h3>
 * <ul>
 *   <li><strong>NBT存储</strong> - 将存储数据保存为NBT格式</li>
 *   <li><strong>版本兼容</strong> - 支持不同版本的数据格式</li>
 *   <li><strong>数据验证</strong> - 验证数据的完整性和有效性</li>
 * </ul>
 * 
 * <h3>网络同步</h3>
 * <ul>
 *   <li><strong>变化检测</strong> - 检测存储内容的变化</li>
 *   <li><strong>增量更新</strong> - 只同步变化的部分</li>
 *   <li><strong>批量同步</strong> - 批量处理多个变化</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.api
 * @see codechicken.enderstorage.manager
 */
@NonNullApi
package codechicken.enderstorage.storage;

import net.covers1624.quack.annotation.NonNullApi;
