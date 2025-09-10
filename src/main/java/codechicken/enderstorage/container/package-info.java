/**
 * 容器管理包 - GUI容器和用户界面集成
 * 
 * <p>
 * 本包定义了末影存储设备的容器类，负责处理客户端和服务器端之间的
 * 物品操作同步、交互逻辑和数据一致性管理。
 * </p>
 * 
 * <h2>核心容器类</h2>
 * 
 * <h3>主要组件</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.container.ContainerEnderItemStorage} - 末影物品存储容器</li>
 * </ul>
 * 
 * <h2>容器特性</h2>
 * 
 * <h3>末影物品存储容器</h3>
 * <ul>
 *   <li><strong>物品同步</strong> - 实时同步存储内容到客户端</li>
 *   <li><strong>槽位管理</strong> - 27个存储槽位 + 36个玩家背包槽位</li>
 *   <li><strong>操作验证</strong> - 验证玩家操作权限和合法性</li>
 *   <li><strong>网络优化</strong> - 智能批量更新和增量同步</li>
 * </ul>
 * 
 * <h2>容器设计</h2>
 * 
 * <h3>槽位管理</h3>
 * <p>
 * 容器采用分层槽位管理设计：
 * </p>
 * <pre>{@code
 * // 存储槽位（0-26）
 * for (int i = 0; i < 27; i++) {
 *     addSlot(new Slot(storage, i, x, y));
 * }
 * 
 * // 玩家背包槽位（27-62）
 * for (int i = 0; i < 36; i++) {
 *     addSlot(new Slot(playerInventory, i, x, y));
 * }
 * }</pre>
 * 
 * <h3>数据同步</h3>
 * <p>
 * 容器提供高效的数据同步机制：
 * </p>
 * <ul>
 *   <li><strong>增量同步</strong> - 只同步变化的槽位</li>
 *   <li><strong>批量更新</strong> - 打开时全量同步</li>
 *   <li><strong>智能压缩</strong> - 压缩网络数据包</li>
 * </ul>
 * 
 * <h2>交互逻辑</h2>
 * 
 * <h3>物品操作</h3>
 * <pre>{@code
 * @Override
 * public ItemStack quickMoveStack(Player player, int index) {
 *     Slot slot = slots.get(index);
 *     if (slot.hasItem()) {
 *         ItemStack stack = slot.getItem();
 *         // 智能移动物品在存储和背包之间
 *         if (index < 27) {
 *             // 从存储移动到背包
 *             moveItemStackTo(stack, 27, 63, true);
 *         } else {
 *             // 从背包移动到存储
 *             moveItemStackTo(stack, 0, 27, false);
 *         }
 *     }
 *     return ItemStack.EMPTY;
 * }
 * }</pre>
 * 
 * <h2>安全性</h2>
 * <p>
 * 容器实现了多层次的安全验证：
 * </p>
 * <ul>
 *   <li><strong>权限检查</strong> - 验证玩家访问权限</li>
 *   <li><strong>距离验证</strong> - 检查玩家与容器的距离</li>
 *   <li><strong>状态同步</strong> - 防止客户端伪造状态</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.client.gui
 * @see codechicken.enderstorage.tile
 */
@NonNullApi
package codechicken.enderstorage.container;

import net.covers1624.quack.annotation.NonNullApi;
