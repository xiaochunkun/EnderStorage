package codechicken.enderstorage.api;

import codechicken.enderstorage.manager.EnderStorageManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * 抽象末影存储基类
 * 
 * 这是所有末影存储实现的基础抽象类，定义了末影存储系统的核心接口和行为。
 * 每个末影存储实例都与一个特定的频率关联，相同频率的存储设备将共享数据。
 * 
 * 核心功能：
 * - 管理存储的脏状态（dirty state），用于延迟写入优化
 * - 跟踪存储内容的变更计数，用于客户端同步
 * - 提供抽象方法供具体的存储类型实现
 * - 集成到存储管理器的保存系统中
 * 
 * 设计模式：
 * - 使用模板方法模式，定义通用的状态管理逻辑
 * - 子类负责实现具体的存储类型（物品存储、液体存储等）
 * - 通过频率系统实现存储的唯一标识和共享机制
 * 
 * 生命周期：
 * 1. 由存储管理器根据频率和类型创建实例
 * 2. 从持久化数据中加载存储内容（如果存在）
 * 3. 响应游戏中的读写操作，标记脏状态
 * 4. 在适当时机由管理器保存到磁盘
 * 
 * @author CodeChickenCore团队
 * @since 1.0.0
 */
public abstract class AbstractEnderStorage {

    /** 管理此存储实例的存储管理器引用 */
    public final EnderStorageManager manager;
    
    /** 此存储实例对应的频率标识 */
    public final Frequency freq;
    
    /** 脏标记，指示存储内容是否已被修改需要保存 */
    private boolean dirty;
    
    /** 变更计数器，用于客户端同步检测 */
    private int changeCount;

    /**
     * 构造函数
     * 
     * 初始化末影存储实例，设置管理器引用和频率标识。
     * 新创建的存储实例默认为干净状态，变更计数为0。
     * 
     * @param manager 负责管理此存储的存储管理器
     * @param freq 此存储对应的频率标识
     */
    public AbstractEnderStorage(EnderStorageManager manager, Frequency freq) {
        this.manager = manager;
        this.freq = freq;
    }

    /**
     * 标记存储为脏状态
     * 
     * 当存储内容发生变化时调用此方法，表示需要保存到磁盘。
     * 采用延迟写入策略：首次标记为脏时会请求管理器进行保存，
     * 后续的脏标记只会增加变更计数。客户端不执行保存操作。
     * 
     * 此方法的调用会：
     * 1. 在服务端环境中将存储加入待保存队列
     * 2. 增加变更计数器，用于客户端同步检测
     * 3. 避免重复的保存请求（dirty标记机制）
     */
    public void setDirty() {
        // 客户端不执行保存操作
        if (manager.client) {
            return;
        }

        // 只有在首次变脏时才请求保存
        if (!dirty) {
            dirty = true;
            manager.requestSave(this);
        }
        
        // 每次修改都增加变更计数
        changeCount++;
    }

    /**
     * 将存储标记为干净状态
     * 
     * 在存储数据成功写入磁盘后由存储管理器调用，
     * 表示当前的内存状态与磁盘状态一致。
     */
    public void setClean() {
        dirty = false;
    }

    /**
     * 获取变更计数
     * 
     * 返回此存储实例的累计变更次数，主要用于客户端同步。
     * 当客户端检测到服务端的变更计数与本地不同时，会请求同步数据。
     * 
     * @return 累计变更次数
     */
    public int getChangeCount() {
        return changeCount;
    }

    /**
     * 清空存储内容
     * 
     * 清除此存储实例中的所有数据，恢复到空状态。
     * 具体的清空逻辑由子类实现，可能包括清空物品、液体等。
     * 调用此方法后通常需要调用setDirty()来标记更改。
     */
    public abstract void clearStorage();

    /**
     * 获取存储类型标识
     * 
     * 返回此存储实例的类型标识字符串，用于区分不同类型的存储。
     * 类型标识与存储插件系统配合使用，确保正确的序列化和反序列化。
     * 
     * @return 存储类型标识字符串（如"item"、"liquid"等）
     */
    public abstract String type();

    /**
     * 将存储内容序列化为NBT标签
     * 
     * 将当前存储的所有数据转换为NBT格式，用于持久化保存。
     * 序列化的数据应包含恢复存储状态所需的全部信息。
     * 
     * @param registries 注册表提供器，用于序列化引用注册表中的对象
     * @return 包含存储数据的NBT标签
     */
    public abstract CompoundTag saveToTag(HolderLookup.Provider registries);

    /**
     * 从NBT标签加载存储内容
     * 
     * 从持久化的NBT数据中恢复存储的内容和状态。
     * 此方法通常在存储管理器创建新的存储实例后调用，
     * 用于加载之前保存的数据。
     * 
     * @param tag 包含存储数据的NBT标签
     * @param registries 注册表提供器，用于反序列化引用注册表中的对象
     */
    public abstract void loadFromTag(CompoundTag tag, HolderLookup.Provider registries);
}
