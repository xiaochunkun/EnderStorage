/**
 * EnderStorage 模组的根包
 * 
 * <p>
 * EnderStorage 是一个允许玩家在不同维度间共享物品存储和流体存储的 Minecraft 模组。
 * 通过使用末影箱子(Ender Chest)和末影储罐(Ender Tank)，玩家可以基于颜色频率系统
 * 建立跨维度的存储网络。
 * </p>
 * 
 * <h2>主要功能</h2>
 * <ul>
 *   <li><strong>末影箱子</strong> - 基于颜色频率的物品存储，支持跨维度访问</li>
 *   <li><strong>末影储罐</strong> - 基于颜色频率的流体存储，支持跨维度访问</li>
 *   <li><strong>末影袋</strong> - 便携式末影箱子，可随身携带</li>
 *   <li><strong>频率系统</strong> - 通过三种颜色组合定义独特的存储频率</li>
 * </ul>
 * 
 * <h2>包结构概览</h2>
 * <ul>
 *   <li>{@link codechicken.enderstorage.api} - 对外提供的API接口和抽象类</li>
 *   <li>{@link codechicken.enderstorage.block} - 末影存储方块的实现</li>
 *   <li>{@link codechicken.enderstorage.client} - 客户端相关功能，包括GUI和渲染</li>
 *   <li>{@link codechicken.enderstorage.config} - 模组配置管理</li>
 *   <li>{@link codechicken.enderstorage.container} - 容器和GUI相关</li>
 *   <li>{@link codechicken.enderstorage.init} - 模组初始化和内容注册</li>
 *   <li>{@link codechicken.enderstorage.item} - 末影存储相关物品</li>
 *   <li>{@link codechicken.enderstorage.manager} - 存储管理器和数据持久化</li>
 *   <li>{@link codechicken.enderstorage.misc} - 杂项工具类和组件</li>
 *   <li>{@link codechicken.enderstorage.network} - 网络通信和数据同步</li>
 *   <li>{@link codechicken.enderstorage.plugin} - 存储类型插件实现</li>
 *   <li>{@link codechicken.enderstorage.recipe} - 自定义配方系统</li>
 *   <li>{@link codechicken.enderstorage.storage} - 存储实现类</li>
 *   <li>{@link codechicken.enderstorage.tile} - 方块实体(TileEntity)实现</li>
 * </ul>
 * 
 * <h2>核心概念</h2>
 * <h3>频率系统</h3>
 * <p>
 * 每个末影存储设备都有一个由三种颜色组成的频率标识。相同频率的设备会共享存储空间，
 * 无论它们位于世界的哪个位置或维度。
 * </p>
 * 
 * <h3>存储类型</h3>
 * <p>
 * 支持两种主要的存储类型：
 * </p>
 * <ul>
 *   <li><strong>物品存储</strong> - 用于存储各种物品和方块</li>
 *   <li><strong>流体存储</strong> - 用于存储各种液体</li>
 * </ul>
 * 
 * <h2>设计原则</h2>
 * <ul>
 *   <li><strong>跨维度兼容</strong> - 所有存储设备都支持跨维度访问</li>
 *   <li><strong>插件化架构</strong> - 通过插件系统支持不同的存储类型</li>
 *   <li><strong>数据持久化</strong> - 确保存储数据在服务器重启后保持</li>
 *   <li><strong>网络同步</strong> - 保证客户端和服务端数据一致性</li>
 *   <li><strong>性能优化</strong> - 高效的存储管理和网络通信</li>
 * </ul>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 获取频率为白-白-白的末影箱子存储
 * Frequency frequency = new Frequency(EnumDyeColor.WHITE, EnumDyeColor.WHITE, EnumDyeColor.WHITE);
 * AbstractEnderStorage storage = EnderStorageManager.instance(false).getStorage(frequency, StorageType.ITEMS);
 * 
 * // 访问存储内容
 * IItemHandler itemHandler = storage.getItemHandler();
 * }</pre>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 */
@NonNullApi
package codechicken.enderstorage;

import net.covers1624.quack.annotation.NonNullApi;
