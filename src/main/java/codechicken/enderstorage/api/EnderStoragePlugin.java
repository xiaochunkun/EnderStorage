package codechicken.enderstorage.api;

import codechicken.enderstorage.manager.EnderStorageManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * 末影存储插件接口 - 可扩展的存储类型插件系统
 * 
 * <p>此接口定义了末影存储系统的扩展机制，允许第三方开发者创建自定义的存储类型。
 * 通过实现此接口，可以添加新的存储类型（如物品存储、液体存储、能量存储等），
 * 并与现有的频率系统和跨维度同步机制无缝集成。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li><strong>存储创建</strong>：根据管理器和频率创建存储实例</li>
 *   <li><strong>类型标识</strong>：提供唯一的存储类型标识符</li>
 *   <li><strong>客户端同步</strong>：向客户端发送存储状态信息</li>
 * </ul>
 * 
 * <h3>实现要求：</h3>
 * <ul>
 *   <li>存储类型T必须继承自AbstractEnderStorage</li>
 *   <li>必须提供唯一的StorageType标识符</li>
 *   <li>必须实现客户端数据同步机制</li>
 *   <li>存储实例必须支持NBT序列化和反序列化</li>
 * </ul>
 * 
 * <h3>生命周期：</h3>
 * <p>1. 注册阶段 - 插件在模组初始化时注册到EnderStorageManager<br>
 * 2. 创建阶段 - 当需要特定频率的存储时调用createEnderStorage<br>
 * 3. 同步阶段 - 定期调用sendClientInfo同步数据到客户端</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * public class CustomStoragePlugin implements EnderStoragePlugin<CustomStorage> {
 *     public static final StorageType<CustomStorage> TYPE = new StorageType<>("custom");
 *     
 *     @Override
 *     public CustomStorage createEnderStorage(EnderStorageManager manager, Frequency freq) {
 *         return new CustomStorage(manager, freq);
 *     }
 *     
 *     @Override
 *     public StorageType<CustomStorage> identifier() {
 *         return TYPE;
 *     }
 *     
 *     @Override
 *     public void sendClientInfo(ServerPlayer player, List<CustomStorage> list) {
 *         // 发送自定义同步数据
 *     }
 * }
 * }</pre>
 * 
 * @param <T> 存储类型，必须继承自AbstractEnderStorage
 * @author ChickenBones
 * @since 1.0.0
 * @see AbstractEnderStorage
 * @see StorageType
 * @see EnderStorageManager
 */
public interface EnderStoragePlugin<T extends AbstractEnderStorage> {

    /**
     * 创建末影存储实例
     * 
     * <p>根据给定的管理器和频率创建特定类型的存储实例。此方法在需要访问
     * 特定频率的存储时被调用，应该返回一个完全初始化的存储对象。</p>
     * 
     * <p><strong>注意</strong>：返回的存储实例将被缓存在管理器中，
     * 相同频率的后续访问将直接返回缓存的实例。</p>
     * 
     * @param manager 末影存储管理器，用于处理数据持久化和跨维度同步
     * @param freq 频率标识，决定存储的共享范围和访问权限
     * @return 新创建的存储实例，必须完全初始化并可以立即使用
     */
    T createEnderStorage(EnderStorageManager manager, Frequency freq);

    /**
     * 获取存储类型标识符
     * 
     * <p>返回此插件管理的存储类型的唯一标识符。此标识符用于：</p>
     * <ul>
     *   <li>在存储管理器中注册和查找插件</li>
     *   <li>NBT数据序列化中的类型标记</li>
     *   <li>网络同步时的类型区分</li>
     * </ul>
     * 
     * <p><strong>重要</strong>：标识符在整个应用生命周期中必须保持一致，
     * 且不能与其他插件的标识符冲突。</p>
     * 
     * @return 唯一的存储类型标识符
     */
    StorageType<T> identifier();

    /**
     * 向客户端发送存储信息
     * 
     * <p>将指定的存储列表的相关信息同步到客户端。此方法在以下情况下被调用：</p>
     * <ul>
     *   <li>玩家登录时的初始同步</li>
     *   <li>存储状态发生变化时的增量同步</li>
     *   <li>定期的状态刷新</li>
     * </ul>
     * 
     * <p>实现时应该只发送客户端需要的数据（如显示用的状态信息），
     * 而不是完整的存储内容，以优化网络传输效率。</p>
     * 
     * @param player 目标玩家，数据将发送给此玩家的客户端
     * @param list 需要同步的存储列表，通常是与玩家相关的存储实例
     */
    void sendClientInfo(ServerPlayer player, List<T> list);
}
