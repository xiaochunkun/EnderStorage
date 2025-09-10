package codechicken.enderstorage.storage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.config.EnderStorageConfig;
import codechicken.enderstorage.container.ContainerEnderItemStorage;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.network.EnderStorageSPH;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.lib.util.ArrayUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 末影物品存储实现类
 * 
 * 这是末影存储系统中负责物品存储的核心实现类。它继承自AbstractEnderStorage
 * 并实现了Minecraft的Container接口，提供了完整的物品存储功能。
 * 
 * 核心功能：
 * - 物品容器管理：支持9、27、54三种容量规格
 * - 线程安全访问：所有关键操作都使用同步锁保护
 * - 动态容量调整：根据配置自动调整存储容量
 * - 网络同步：支持客户端和服务端之间的数据同步
 * - 持久化存储：自动保存和加载物品数据
 * - 多玩家支持：跟踪同时访问的玩家数量
 * 
 * 设计特点：
 * - 实现了Minecraft标准的Container接口，完全兼容原版物品处理逻辑
 * - 使用同步机制确保多线程环境下的数据安全性
 * - 支持容量的动态扩展和收缩，保护现有物品不丢失
 * - 集成了开关状态监控，用于客户端动画效果
 * 
 * 生命周期：
 * 1. 通过EnderStorageManager根据频率创建实例
 * 2. 从NBT数据中加载持久化的物品内容
 * 3. 响应玩家的物品操作请求
 * 4. 在数据变更时自动标记为脏状态并保存
 * 
 * @author CodeChickenCore团队
 * @since 1.0.0
 */
public class EnderItemStorage extends AbstractEnderStorage implements Container {

    /** 物品存储类型标识符，用于存储管理器中的类型识别 */
    public static final StorageType<EnderItemStorage> TYPE = new StorageType<>("item");

    /** 支持的存储容量数组：小(9格)、中(27格)、大(54格) */
    public static final int[] sizes = new int[] { 9, 27, 54 };

    /** 当前存储容量等级 (0=9格, 1=27格, 2=54格) */
    private int size;
    
    /** 物品数组，存储实际的物品堆叠 */
    private ItemStack[] items;
    
    /** 当前打开此存储的玩家数量，用于开关状态同步 */
    private int open;

    /**
     * 构造函数
     * 
     * 创建一个新的末影物品存储实例，初始化容量和物品数组。
     * 容量大小根据配置文件中的设置确定，物品数组使用空物品堆叠填充。
     * 
     * @param manager 管理此存储的存储管理器实例
     * @param freq 此存储对应的频率标识
     */
    public EnderItemStorage(EnderStorageManager manager, Frequency freq) {
        super(manager, freq);
        // 从配置中获取默认容量等级
        size = EnderStorageConfig.storageSize;
        // 创建物品数组并用空物品堆叠填充
        items = ArrayUtils.fill(new ItemStack[getContainerSize()], ItemStack.EMPTY);
    }

    /**
     * 清空存储内容
     * 
     * 清除存储中的所有物品，将所有槽位重置为空。
     * 此操作是线程安全的，并会自动标记存储为脏状态以触发保存。
     */
    @Override
    public void clearStorage() {
        synchronized (this) {
            empty();
            setDirty();
        }
    }

    /**
     * 从NBT标签加载存储数据
     * 
     * 从持久化的NBT数据中恢复物品存储的内容和配置。
     * 加载完成后会检查容量配置是否发生变化，如有需要则调整容量。
     * 
     * @param tag 包含存储数据的NBT标签
     * @param registries 注册表提供器，用于反序列化物品
     */
    @Override
    public void loadFromTag(CompoundTag tag, HolderLookup.Provider registries) {
        // 读取保存的容量等级
        size = tag.getByte("size");
        // 清空当前物品数组
        empty();
        // 从NBT数据中恢复物品堆叠
        InventoryUtils.readItemStacksFromTag(registries, items, tag.getList("Items", 10));
        // 如果配置中的容量与保存的容量不同，则调整容量
        if (size != EnderStorageConfig.storageSize) {
            alignSize();
        }
    }

    /**
     * 调整存储容量
     * 
     * 根据配置文件中的新容量设置调整物品存储的大小。此方法处理两种情况：
     * 1. 扩容：直接创建更大的数组并复制现有物品
     * 2. 缩容：检查现有物品数量，如果能容纳则压缩数组，否则保持原容量
     * 
     * 缩容策略确保不会丢失玩家的物品，只有在新容量足够容纳所有现有物品时才执行缩容。
     */
    private void alignSize() {
        if (EnderStorageConfig.storageSize > size) {
            // 扩容：创建更大的物品数组
            ItemStack[] newItems = ArrayUtils.fill(new ItemStack[sizes[EnderStorageConfig.storageSize]], ItemStack.EMPTY);
            // 复制现有物品到新数组
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
            size = EnderStorageConfig.storageSize;
            setChanged();
        } else {
            // 缩容：先检查现有物品数量
            int numStacks = 0;
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    numStacks++;
                }
            }

            // 只有新容量能容纳所有现有物品时才执行缩容
            if (numStacks <= sizes[EnderStorageConfig.storageSize]) {
                ItemStack[] newItems = ArrayUtils.fill(new ItemStack[sizes[EnderStorageConfig.storageSize]], ItemStack.EMPTY);
                int copyTo = 0;
                // 压缩数组，将非空物品移动到新数组的前面
                for (ItemStack item : items) {
                    if (!item.isEmpty()) {
                        newItems[copyTo] = item;
                        copyTo++;
                    }
                }
                items = newItems;
                size = EnderStorageConfig.storageSize;
                setChanged();
            }
        }
    }

    /**
     * 获取存储类型标识符
     * 
     * 返回此存储实例的类型标识字符串，用于存储管理器中的类型识别和序列化。
     * 
     * @return 存储类型标识符 "item"
     */
    @Override
    public String type() {
        return "item";
    }

    /**
     * 将存储数据保存为NBT标签
     * 
     * 将当前存储的所有物品和配置信息序列化为NBT格式，用于持久化保存。
     * 在保存前会检查是否需要调整容量（仅在没有玩家打开时执行）。
     * 
     * @param registries 注册表提供器，用于序列化物品引用
     * @return 包含存储数据的NBT标签
     */
    @Override
    public CompoundTag saveToTag(HolderLookup.Provider registries) {
        // 如果容量配置发生变化且当前没有玩家打开，则调整容量
        if (size != EnderStorageConfig.storageSize && open == 0) {
            alignSize();
        }

        CompoundTag compound = new CompoundTag();
        // 序列化物品数组
        compound.put("Items", InventoryUtils.writeItemStacksToTag(registries, items));
        // 保存容量等级
        compound.putByte("size", (byte) size);

        return compound;
    }

    /**
     * 获取指定槽位的物品堆叠（线程安全）
     * 
     * 返回指定槽位中的物品堆叠。此方法是线程安全的，
     * 可以在多线程环境下安全调用。
     * 
     * @param slot 槽位索引
     * @return 该槽位中的物品堆叠
     */
    public ItemStack getItem(int slot) {
        synchronized (this) {
            return items[slot];
        }
    }

    public ItemStack removeItemNoUpdate(int slot) {
        synchronized (this) {
            return InventoryUtils.removeStackFromSlot(this, slot);
        }
    }

    public void setItem(int slot, ItemStack stack) {
        synchronized (this) {
            items[slot] = stack;
            setChanged();
        }
    }

    public void openInventory() {
        if (manager.client) {
            return;
        }

        synchronized (this) {
            open++;
            if (open == 1) {
                EnderStorageSPH.sendOpenUpdateTo(null, freq, true);
            }
        }
    }

    public void closeInventory() {
        if (manager.client) {
            return;
        }

        synchronized (this) {
            open--;
            if (open == 0) {
                EnderStorageSPH.sendOpenUpdateTo(null, freq, false);
            }
        }
    }

    public int getNumOpen() {
        return open;
    }

    @Override
    public int getContainerSize() {
        return sizes[size];
    }

    @Override
    public boolean isEmpty() {
        return ArrayUtils.count(items, (stack -> !stack.isEmpty())) <= 0;
    }

    public ItemStack removeItem(int slot, int size) {
        synchronized (this) {
            return InventoryUtils.decrStackSize(this, slot, size);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        setDirty();
    }

    @Override
    public boolean stillValid(Player var1) {
        return true;
    }

    public void empty() {
        items = new ItemStack[getContainerSize()];
        ArrayUtils.fill(items, ItemStack.EMPTY);
    }

    public void openContainer(ServerPlayer player, Component title) {
        CCLMenuType.openMenu(player, new SimpleMenuProvider((id, inv, p) -> new ContainerEnderItemStorage(id, inv, EnderItemStorage.this), title),
                packet -> {
                    freq.writeToPacket(packet);
                    packet.writeByte(size);
                });
    }

    public void handleContainerPacket(MCDataInput packet) {
        size = packet.readByte();
        empty();
    }

    public int getSize() {
        return size;
    }

    public int openCount() {
        return open;
    }

    public void setClientOpen(int i) {
        if (manager.client) {
            open = i;
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }

    @Override
    public void clearContent() {
    }
}
