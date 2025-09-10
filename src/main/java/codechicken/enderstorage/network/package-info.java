/**
 * 网络通信包 - 客户端和服务器端数据同步
 * 
 * <p>
 * 本包负责 EnderStorage 模组的所有网络通信功能，包括数据包定义、
 * 网络处理程序、数据同步机制以及网络优化策略。
 * </p>
 * 
 * <h2>核心网络类</h2>
 * 
 * <h3>主要组件</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.network.EnderStorageNetwork} - 网络通信主管理器</li>
 *   <li>{@link codechicken.enderstorage.network.EnderStorageCPH} - 客户端数据包处理程序</li>
 *   <li>{@link codechicken.enderstorage.network.EnderStorageSPH} - 服务器端数据包处理程序</li>
 *   <li>{@link codechicken.enderstorage.network.TankSynchroniser} - 储罐数据同步器</li>
 * </ul>
 * 
 * <h2>网络架构</h2>
 * 
 * <h3>数据包类型</h3>
 * <ul>
 *   <li><strong>存储同步</strong> - 同步存储内容到客户端</li>
 *   <li><strong>频率更新</strong> - 同步频率变化</li>
 *   <li><strong>状态同步</strong> - 同步设备状态信息</li>
 *   <li><strong>权限验证</strong> - 验证玩家操作权限</li>
 * </ul>
 * 
 * <h3>同步机制</h3>
 * <ul>
 *   <li><strong>增量同步</strong> - 只同步变化的数据</li>
 *   <li><strong>批量更新</strong> - 将多个更新打包成一个数据包</li>
 *   <li><strong>压缩传输</strong> - 对大数据包进行压缩</li>
 *   <li><strong>错误恢复</strong> - 网络错误时的数据恢复</li>
 * </ul>
 * 
 * <h2>性能优化</h2>
 * 
 * <h3>网络优化</h3>
 * <ul>
 *   <li><strong>带宽管理</strong> - 智能管理网络带宽使用</li>
 *   <li><strong>分片传输</strong> - 大数据分片传输</li>
 *   <li><strong>队列管理</strong> - 优先级队列管理数据包</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.manager
 */
@NonNullApi
package codechicken.enderstorage.network;

import net.covers1624.quack.annotation.NonNullApi;
