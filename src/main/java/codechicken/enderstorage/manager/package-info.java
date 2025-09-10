/**
 * 存储管理器包 - 中央化存储管理和数据持久化
 * 
 * <p>
 * 本包包含了 EnderStorage 模组的核心存储管理系统，负责管理所有
 * 末影存储实例、数据持久化、缓存策略和跨维度同步。
 * </p>
 * 
 * <h2>核心管理器</h2>
 * 
 * <h3>主要组件</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.manager.EnderStorageManager} - 中央存储管理器</li>
 * </ul>
 * 
 * <h2>管理器特性</h2>
 * 
 * <h3>存储管理</h3>
 * <ul>
 *   <li><strong>实例管理</strong> - 管理所有存储实例的生命周期</li>
 *   <li><strong>频率映射</strong> - 维护频率到存储实例的映射关系</li>
 *   <li><strong>类型支持</strong> - 支持多种存储类型（物品、流体）</li>
 *   <li><strong>动态加载</strong> - 按需加载存储实例，节省内存</li>
 * </ul>
 * 
 * <h3>数据持久化</h3>
 * <ul>
 *   <li><strong>世界数据</strong> - 将存储数据保存到世界文件中</li>
 *   <li><strong>增量保存</strong> - 只保存变化的存储数据</li>
 *   <li><strong>备份机制</strong> - 提供数据备份和恢复功能</li>
 *   <li><strong>版本兼容</strong> - 支持不同版本数据格式迁移</li>
 * </ul>
 * 
 * <h2>管理器架构</h2>
 * 
 * <h3>单例设计</h3>
 * <p>
 * 管理器采用单例模式，分别为客户端和服务器端提供独立的实例：
 * </p>
 * <pre>{@code
 * // 获取服务器端管理器
 * EnderStorageManager serverManager = EnderStorageManager.instance(false);
 * 
 * // 获取客户端管理器
 * EnderStorageManager clientManager = EnderStorageManager.instance(true);
 * 
 * // 获取或创建存储实例
 * AbstractEnderStorage storage = manager.getStorage(frequency, StorageType.ITEMS);
 * }</pre>
 * 
 * <h3>缓存策略</h3>
 * <p>
 * 实现了智能缓存系统来优化性能：
 * </p>
 * <ul>
 *   <li><strong>LRU 缓存</strong> - 最近最少使用算法管理内存</li>
 *   <li><strong>懒加载</strong> - 只在需要时加载存储数据</li>
 *   <li><strong>定时清理</strong> - 定期清理未使用的缓存</li>
 *   <li><strong>内存监控</strong> - 监控内存使用情况防止溢出</li>
 * </ul>
 * 
 * <h2>网络同步</h2>
 * 
 * <h3>客户端同步</h3>
 * <p>
 * 管理器负责客户端和服务器端的数据同步：
 * </p>
 * <pre>{@code
 * // 同步存储内容到客户端
 * public void syncStorageToClient(ServerPlayer player, Frequency frequency) {
 *     AbstractEnderStorage storage = getStorage(frequency, StorageType.ITEMS);
 *     PacketHandler.sendToPlayer(new SyncStoragePacket(storage), player);
 * }
 * 
 * // 处理客户端同步数据
 * public void handleSyncPacket(SyncStoragePacket packet) {
 *     AbstractEnderStorage storage = getStorage(packet.getFrequency(), packet.getType());
 *     storage.readFromNBT(packet.getData());
 * }
 * }</pre>
 * 
 * <h3>增量更新</h3>
 * <p>
 * 优化的增量更新机制减少网络开销：
 * </p>
 * <ul>
 *   <li><strong>变化检测</strong> - 检测存储内容的具体变化</li>
 *   <li><strong>批量更新</strong> - 将多个变化打包成一个网络包</li>
 *   <li><strong>压缩传输</strong> - 对大量数据进行压缩传输</li>
 * </ul>
 * 
 * <h2>事件系统</h2>
 * 
 * <h3>存储事件</h3>
 * <p>
 * 管理器提供了丰富的事件系统：
 * </p>
 * <pre>{@code
 * // 注册存储事件监听器
 * manager.addStorageListener(new IStorageListener() {
 *     @Override
 *     public void onStorageLoaded(AbstractEnderStorage storage) {
 *         // 存储加载完成
 *     }
 *     
 *     @Override
 *     public void onStorageChanged(AbstractEnderStorage storage) {
 *         // 存储内容发生变化
 *     }
 *     
 *     @Override
 *     public void onStorageUnloaded(AbstractEnderStorage storage) {
 *         // 存储被卸载
 *     }
 * });
 * }</pre>
 * 
 * <h2>性能优化</h2>
 * 
 * <h3>内存管理</h3>
 * <ul>
 *   <li><strong>对象池</strong> - 重用存储实例对象减少 GC 压力</li>
 *   <li><strong>软引用</strong> - 使用软引用防止内存泄漏</li>
 *   <li><strong>定时清理</strong> - 定期清理无用数据</li>
 * </ul>
 * 
 * <h3>线程安全</h3>
 * <ul>
 *   <li><strong>读写锁</strong> - 使用读写锁保证并发安全</li>
 *   <li><strong>原子操作</strong> - 关键操作使用原子类型</li>
 *   <li><strong>无锁算法</strong> - 部分热点操作使用无锁算法</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.api
 * @see codechicken.enderstorage.storage
 * @see codechicken.enderstorage.network
 */
@NonNullApi
package codechicken.enderstorage.manager;

import net.covers1624.quack.annotation.NonNullApi;
