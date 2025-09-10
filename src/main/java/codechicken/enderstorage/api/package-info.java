/**
 * EnderStorage API包 - 对外提供的应用程序接口
 * 
 * <p>
 * 本包包含了 EnderStorage 模组对外提供的所有API接口、抽象类和核心数据结构。
 * 其他模组可以通过这些接口来集成和扩展 EnderStorage 的功能。
 * </p>
 * 
 * <h2>核心组件</h2>
 * 
 * <h3>主要类和接口</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.api.Frequency} - 频率数据结构，定义存储设备的颜色组合</li>
 *   <li>{@link codechicken.enderstorage.api.AbstractEnderStorage} - 抽象存储类，所有末影存储的基类</li>
 *   <li>{@link codechicken.enderstorage.api.StorageType} - 存储类型枚举，定义不同的存储种类</li>
 *   <li>{@link codechicken.enderstorage.api.EnderStoragePlugin} - 存储插件接口，用于扩展新的存储类型</li>
 * </ul>
 * 
 * <h2>频率系统设计</h2>
 * <p>
 * 频率系统是 EnderStorage 的核心机制，每个存储设备都通过一个由三种颜色组成的频率来标识。
 * 相同频率的设备会共享存储空间，无论它们在世界中的位置或所在的维度。
 * </p>
 * 
 * <h3>频率结构</h3>
 * <pre>{@code
 * Frequency frequency = new Frequency(
 *     EnumDyeColor.WHITE,  // 左侧颜色
 *     EnumDyeColor.WHITE,  // 中间颜色  
 *     EnumDyeColor.WHITE   // 右侧颜色
 * );
 * }</pre>
 * 
 * <h2>存储类型架构</h2>
 * <p>
 * EnderStorage 采用插件化的存储类型设计，目前支持以下类型：
 * </p>
 * <ul>
 *   <li><strong>ITEMS</strong> - 物品存储，用于存储各种游戏物品</li>
 *   <li><strong>LIQUID</strong> - 流体存储，用于存储各种流体</li>
 * </ul>
 * 
 * <h2>API使用示例</h2>
 * 
 * <h3>获取存储实例</h3>
 * <pre>{@code
 * // 创建频率
 * Frequency frequency = new Frequency(EnumDyeColor.RED, EnumDyeColor.GREEN, EnumDyeColor.BLUE);
 * 
 * // 获取物品存储
 * AbstractEnderStorage itemStorage = EnderStorageManager.instance(false)
 *     .getStorage(frequency, StorageType.ITEMS);
 * 
 * // 获取流体存储
 * AbstractEnderStorage liquidStorage = EnderStorageManager.instance(false)
 *     .getStorage(frequency, StorageType.LIQUID);
 * }</pre>
 * 
 * <h3>自定义存储插件</h3>
 * <pre>{@code
 * public class MyStoragePlugin implements EnderStoragePlugin {
 *     @Override
 *     public AbstractEnderStorage createStorage(EnderStorageManager manager, Frequency frequency) {
 *         return new MyCustomStorage(manager, frequency);
 *     }
 *     
 *     @Override
 *     public void onStorageLoaded(AbstractEnderStorage storage) {
 *         // 存储加载后的初始化逻辑
 *     }
 * }
 * }</pre>
 * 
 * <h2>设计原则</h2>
 * <ul>
 *   <li><strong>扩展性</strong> - 通过插件接口支持新的存储类型</li>
 *   <li><strong>一致性</strong> - 所有存储类型遵循统一的接口设计</li>
 *   <li><strong>线程安全</strong> - API设计考虑了多线程环境下的安全性</li>
 *   <li><strong>向后兼容</strong> - 保持API的向后兼容性</li>
 * </ul>
 * 
 * <h2>集成指南</h2>
 * <p>
 * 其他模组集成 EnderStorage API 时，建议遵循以下步骤：
 * </p>
 * <ol>
 *   <li>添加 EnderStorage 作为依赖</li>
 *   <li>实现 {@link codechicken.enderstorage.api.EnderStoragePlugin} 接口</li>
 *   <li>在适当的时机注册自定义存储类型</li>
 *   <li>使用 {@link codechicken.enderstorage.api.Frequency} 管理存储频率</li>
 * </ol>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.manager.EnderStorageManager
 * @see codechicken.enderstorage.storage
 */
@NonNullApi
package codechicken.enderstorage.api;

import net.covers1624.quack.annotation.NonNullApi;
