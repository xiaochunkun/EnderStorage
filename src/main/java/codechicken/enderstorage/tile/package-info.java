/**
 * 方块实体包 - TileEntity/BlockEntity 实现
 * 
 * <p>
 * 本包包含了所有末影存储方块的 TileEntity 实现，负责处理方块的逻辑、
 * 数据存储、网络同步以及与其他模组的集成。
 * </p>
 * 
 * <h2>核心方块实体</h2>
 * 
 * <h3>主要TileEntity实现</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.tile.TileEnderChest} - 末影箱子方块实体</li>
 *   <li>{@link codechicken.enderstorage.tile.TileEnderTank} - 末影储罐方块实体</li>
 *   <li>{@link codechicken.enderstorage.tile.TileFrequencyOwner} - 频率所有者基类</li>
 * </ul>
 * 
 * <h2>方块实体特性</h2>
 * 
 * <h3>末影箱子实体</h3>
 * <ul>
 *   <li><strong>存储管理</strong> - 管理物品存储和访问</li>
 *   <li><strong>动画控制</strong> - 控制箱子开合动画</li>
 *   <li><strong>音效管理</strong> - 播放开关声音效果</li>
 *   <li><strong>玩家交互</strong> - 处理玩家的交互操作</li>
 * </ul>
 * 
 * <h3>末影储罐实体</h3>
 * <ul>
 *   <li><strong>流体存储</strong> - 管理流体存储和传输</li>
 *   <li><strong>液位显示</strong> - 显示当前流体液位</li>
 *   <li><strong>红石支持</strong> - 红石比较器输出</li>
 *   <li><strong>管道连接</strong> - 与流体管道系统的连接</li>
 * </ul>
 * 
 * <h2>数据管理</h2>
 * 
 * <h3>持久化机制</h3>
 * <ul>
 *   <li><strong>NBT存储</strong> - 使用NBT保存方块数据</li>
 *   <li><strong>频率信息</strong> - 保存频率和所有者信息</li>
 *   <li><strong>配置设置</strong> - 保存私人模式等设置</li>
 * </ul>
 * 
 * <h3>网络同步</h3>
 * <ul>
 *   <li><strong>客户端同步</strong> - 实时同步数据到客户端</li>
 *   <li><strong>视觉更新</strong> - 触发客户端视觉更新</li>
 *   <li><strong>状态改变</strong> - 通知客户端状态变化</li>
 * </ul>
 * 
 * <h2>Tick系统</h2>
 * 
 * <h3>更新机制</h3>
 * <ul>
 *   <li><strong>定时更新</strong> - 定期检查和更新状态</li>
 *   <li><strong>变化检测</strong> - 检测数据变化并响应</li>
 *   <li><strong>性能优化</strong> - 避免不必要的计算</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.block
 * @see codechicken.enderstorage.storage
 */
@NonNullApi
package codechicken.enderstorage.tile;

import net.covers1624.quack.annotation.NonNullApi;
