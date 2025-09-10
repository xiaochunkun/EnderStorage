package codechicken.enderstorage.manager;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.lib.util.ServerUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * 末影存储管理器
 * 
 * 这是末影存储系统的核心管理类，负责：
 * 1. 管理所有末影存储实例的生命周期
 * 2. 处理存储数据的持久化保存和加载
 * 3. 协调客户端和服务端的存储同步
 * 4. 管理存储插件系统
 * 5. 处理玩家登录和维度切换时的数据同步
 * 
 * 存储管理采用双缓冲机制确保数据安全：
 * - data1.dat 和 data2.dat 作为主要数据文件
 * - lock.dat 记录当前使用的数据文件
 * - 写入时先写入备用文件，成功后再更新lock文件
 * 
 * @author CodeChickenCore团队
 * @since 1.0.0
 */

public class EnderStorageManager {

    /**
     * 末影存储保存事件处理器
     * 
     * 监听各种游戏事件，在适当时机触发存储数据的保存和同步：
     * - 世界加载时重新初始化管理器
     * - 世界保存时将脏数据写入磁盘
     * - 玩家登录时发送客户端同步信息
     * - 玩家切换维度时重新同步数据
     */
    public static class EnderStorageSaveHandler {

        /**
         * 监听世界加载事件
         * 当客户端世界加载时，重新初始化客户端存储管理器
         * 
         * @param event 世界加载事件
         */
        @SubscribeEvent
        public void onWorldLoad(LevelEvent.Load event) {
            if (event.getLevel().isClientSide()) {
                reloadManager(true);
            }
        }

        /**
         * 监听世界保存事件
         * 当服务端世界保存时，将所有脏存储数据写入磁盘
         * 
         * @param event 世界保存事件
         */
        @SubscribeEvent
        public void onWorldSave(LevelEvent.Save event) {
            if (!event.getLevel().isClientSide() && instance(false) != null) {
                instance(false).save(false);
            }
        }

        /**
         * 监听玩家登录事件
         * 当玩家登录服务器时，向客户端发送存储同步信息
         * 
         * @param event 玩家登录事件
         */
        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            instance(false).sendClientInfo((ServerPlayer) event.getEntity());
        }

        /**
         * 监听玩家维度切换事件
         * 当玩家切换维度时，重新向客户端发送存储同步信息
         * 
         * @param event 玩家维度切换事件
         */
        @SubscribeEvent
        public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            instance(false).sendClientInfo((ServerPlayer) event.getEntity());
        }
    }

    /** 服务端存储管理器实例 */
    private static @Nullable EnderStorageManager serverManager;
    
    /** 客户端存储管理器实例 */
    private static @Nullable EnderStorageManager clientManager;
    
    /** 存储插件注册表，映射存储类型到对应的插件实现 */
    private static Map<StorageType<?>, EnderStoragePlugin<?>> plugins = new HashMap<>();

    /** 存储实例映射表，通过频率和类型的组合键查找存储实例 */
    private Map<String, AbstractEnderStorage> storageMap;
    
    /** 按存储类型分组的存储实例列表 */
    private Map<StorageType<?>, List<AbstractEnderStorage>> storageList;
    
    /** 标识当前实例是否为客户端管理器 */
    public final boolean client;

    /** 存储数据保存目录 */
    private File saveDir;
    
    /** 存储数据文件数组 [data1.dat, data2.dat, lock.dat] */
    private File[] saveFiles;
    
    /** 当前要写入的数据文件索引 (0或1) */
    private int saveTo;
    
    /** 需要保存的脏存储实例列表 */
    private List<AbstractEnderStorage> dirtyStorage;
    
    /** NBT标签，用于序列化存储数据 */
    private CompoundTag saveTag;

    /**
     * 存储管理器构造函数
     * 
     * 初始化存储管理器实例，设置必要的数据结构和文件系统:
     * - 创建线程安全的存储映射表和列表
     * - 为每个注册的存储类型创建存储列表
     * - 如果是服务端实例，则加载持久化数据
     * 
     * @param client 是否为客户端实例，true表示客户端，false表示服务端
     */
    public EnderStorageManager(boolean client) {
        this.client = client;

        // 初始化线程安全的数据结构
        storageMap = Collections.synchronizedMap(new HashMap<>());
        storageList = Collections.synchronizedMap(new HashMap<>());
        dirtyStorage = Collections.synchronizedList(new LinkedList<>());

        // 为每个已注册的插件创建存储列表
        for (StorageType<?> key : plugins.keySet()) {
            storageList.put(key, new ArrayList<>());
        }

        // 仅服务端需要加载持久化数据
        if (!client) {
            load();
        }
    }

    /**
     * 初始化存储管理器系统
     * 注册服务器启动事件监听器，在服务器启动时自动创建服务端管理器实例
     */
    public static void init() {
        NeoForge.EVENT_BUS.addListener(EnderStorageManager::onServerStarted);
    }

    /**
     * 服务器启动事件处理函数
     * 当服务器启动完成时，创建服务端存储管理器实例
     * 
     * @param event 服务器启动事件
     */
    private static void onServerStarted(ServerStartedEvent event) {
        EnderStorageManager.reloadManager(false);
    }

    /**
     * 向客户端发送存储同步信息
     * 
     * 遍历所有已注册的存储插件，调用各插件的同步方法将
     * 服务端存储数据发送给指定的客户端玩家
     * 
     * @param player 需要接收同步数据的服务端玩家实例
     */
    private void sendClientInfo(ServerPlayer player) {
        for (Map.Entry<StorageType<?>, EnderStoragePlugin<?>> plugin : plugins.entrySet()) {
            plugin.getValue().sendClientInfo(player, unsafeCast(storageList.get(plugin.getKey())));
        }
    }

    /**
     * 不安全的类型转换方法
     * 
     * 由于Java泛型擦除的限制，在某些情况下需要进行不安全的类型转换。
     * 此方法用于绕过编译时的类型检查，调用时需要确保类型安全。
     * 
     * @param object 需要转换的对象
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    @SuppressWarnings ("unchecked")
    private static <T> T unsafeCast(Object object) {
        return (T) object;
    }

    /**
     * 加载持久化的存储数据
     * 
     * 从磁盘中加载之前保存的存储数据。采用双缓冲机制确保数据安全：
     * 
     * 文件结构：
     * - data1.dat / data2.dat: 两个交替使用的主数据文件
     * - lock.dat: 记录当前有效数据文件的标识
     * 
     * 加载流程：
     * 1. 创建保存目录（如果不存在）
     * 2. 检查lock.dat文件确定当前有效的数据文件
     * 3. 从有效的数据文件中加载NBT数据
     * 4. 如果加载失败，抛出详细的错误信息
     * 
     * @throws RuntimeException 当数据文件损坏无法读取时
     */
    private void load() {
        // 获取保存目录路径
        saveDir = new File(ServerUtils.getSaveDirectory().toFile(), "EnderStorage");
        try {
            // 确保保存目录存在
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            // TODO: 这看起来可能比较复杂，但实际上是一个很智能的设计
            // data1和data2本质上是备份文件，lock保存当前数据，
            // 并且lock只在成功写入data1/2后才会更新
            // TODO: 也许这不再是必要的？也许应该通过WorldSavedData存储..
            saveFiles = new File[] { 
                new File(saveDir, "data1.dat"), 
                new File(saveDir, "data2.dat"), 
                new File(saveDir, "lock.dat") 
            };
            
            // 检查lock文件是否存在且非空
            if (saveFiles[2].exists() && saveFiles[2].length() > 0) {
                FileInputStream fin = new FileInputStream(saveFiles[2]);
                // 读取当前使用的数据文件索引，并计算下次要写入的文件索引
                saveTo = fin.read() ^ 1;
                fin.close();

                // 从当前有效的数据文件中加载数据
                if (saveFiles[saveTo ^ 1].exists()) {
                    FileInputStream in = new FileInputStream(saveFiles[saveTo ^ 1]);
                    saveTag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                    in.close();
                } else {
                    // 如果数据文件不存在，创建空的NBT标签
                    saveTag = new CompoundTag();
                }
            } else {
                // 如果lock文件不存在，说明是第一次运行，初始化空数据
                saveTag = new CompoundTag();
            }
        } catch (Exception e) {
            // 如果加载失败，抛出详细的错误信息和解决方案
            throw new RuntimeException(String.format(
                "EnderStorage was unable to read it's data, please delete the 'EnderStorage' folder Here: %s and start the server again.", 
                saveDir), e);
        }
    }

    /**
     * 将存储数据保存到磁盘
     * 
     * 将所有标记为脏的存储实例的数据序列化并写入磁盘。
     * 采用原子写入机制确保数据一致性：
     * 
     * 保存流程：
     * 1. 遍历所有脏存储，将其数据序列化到NBT标签
     * 2. 将NBT数据先写入备用数据文件
     * 3. 写入成功后，更新lock文件指向新的数据文件
     * 4. 切换下一次要使用的数据文件索引
     * 
     * @param force 是否强制保存，即使没有脏数据也保存
     * @throws RuntimeException 当写入文件失败时
     */
    private void save(boolean force) {
        // 只有存在脏数据或强制保存时才执行保存操作
        if (!dirtyStorage.isEmpty() || force) {
            // 遍历所有脏存储，将其数据序列化到saveTag
            for (AbstractEnderStorage inv : dirtyStorage) {
                // 使用频率和类型作为键，存储对应的NBT数据
                saveTag.put(inv.freq + ",type=" + inv.type(), 
                           inv.saveToTag(ServerLifecycleHooks.getCurrentServer().registryAccess()));
                // 标记为干净状态
                inv.setClean();
            }

            // 清空脏数据列表
            dirtyStorage.clear();

            try {
                // 获取当前要写入的数据文件
                File saveFile = saveFiles[saveTo];
                if (!saveFile.exists()) {
                    saveFile.createNewFile();
                }
                
                // 将NBT数据压缩写入数据文件
                DataOutputStream dout = new DataOutputStream(new FileOutputStream(saveFile));
                NbtIo.writeCompressed(saveTag, dout);
                dout.close();
                
                // 数据写入成功后，更新lock文件
                FileOutputStream fout = new FileOutputStream(saveFiles[2]);
                fout.write(saveTo);
                fout.close();
                
                // 切换下一次要使用的数据文件索引（0和1之间切换）
                saveTo ^= 1;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 重新加载存储管理器
     * 
     * 创建新的存储管理器实例来替换当前的实例。
     * 此操作通常在世界加载或服务器启动时进行。
     * 
     * @param client 是否为客户端管理器，true表示客户端，false表示服务端
     */
    public static void reloadManager(boolean client) {
        EnderStorageManager newManager = new EnderStorageManager(client);
        if (client) {
            clientManager = newManager;
        } else {
            serverManager = newManager;
        }
    }

    /**
     * 获取保存目录
     * 
     * @return 存储数据的保存目录
     */
    public File getSaveDir() {
        return saveDir;
    }

    /**
     * 获取存储管理器实例
     * 
     * 根据指定的环境（客户端/服务端）返回对应的存储管理器实例。
     * 如果实例不存在，会自动创建一个新实例。
     * 
     * @param client 是否获取客户端实例，true表示客户端，false表示服务端
     * @return 对应的存储管理器实例
     */
    public static EnderStorageManager instance(boolean client) {
        EnderStorageManager manager = client ? clientManager : serverManager;
        if (manager == null) {
            reloadManager(client);
            manager = client ? clientManager : serverManager;
        }
        return manager;
    }

    /**
     * 获取指定频率和类型的存储实例
     * 
     * 根据频率和存储类型查找或创建对应的存储实例。
     * 如果存储实例不存在，会使用对应的插件创建新实例。
     * 在服务端环境中，还会尝试从持久化数据中加载存储内容。
     * 
     * @param freq 存储频率，由三个颜色组成的唯一标识
     * @param type 存储类型，指定存储的具体类型（物品/液体）
     * @param <T> 存储实例的类型
     * @return 对应的存储实例
     */
    @SuppressWarnings ("unchecked")
    public <T extends AbstractEnderStorage> T getStorage(Frequency freq, StorageType<T> type) {
        // 构建存储键：频率 + 类型
        String key = freq + ",type=" + type.name();
        AbstractEnderStorage storage = storageMap.get(key);
        
        if (storage == null) {
            // 存储实例不存在，使用插件创建新实例
            storage = plugins.get(type).createEnderStorage(this, freq);
            
            // 在服务端且持久化数据中存在该存储的数据时，加载数据
            if (!client && saveTag.contains(key)) {
                storage.loadFromTag(saveTag.getCompound(key), 
                                   ServerLifecycleHooks.getCurrentServer().registryAccess());
            }
            
            // 将新创建的存储添加到管理结构中
            storageMap.put(key, storage);
            storageList.get(type).add(storage);
        }
        
        return (T) storage;
    }

    /**
     * 注册存储插件
     * 
     * 将新的存储插件注册到系统中，并为已存在的管理器实例
     * 初始化对应的存储列表。
     * 
     * @param plugin 要注册的存储插件
     */
    public static void registerPlugin(EnderStoragePlugin<?> plugin) {
        plugins.put(plugin.identifier(), plugin);

        // 为已存在的管理器实例初始化新插件的存储列表
        if (serverManager != null) {
            serverManager.storageList.put(plugin.identifier(), new ArrayList<>());
        }
        if (clientManager != null) {
            clientManager.storageList.put(plugin.identifier(), new ArrayList<>());
        }
    }

    /**
     * 获取指定类型的存储插件
     * 
     * @param identifier 存储类型标识符
     * @return 对应的存储插件实例，如果不存在则返回null
     */
    public static EnderStoragePlugin<?> getPlugin(StorageType<?> identifier) {
        return plugins.get(identifier);
    }

    /**
     * 获取所有已注册的存储插件
     * 
     * @return 不可变的插件映射表副本
     */
    public static Map<StorageType<?>, EnderStoragePlugin<?>> getPlugins() {
        return ImmutableMap.copyOf(plugins);
    }

    /**
     * 获取指定类型的所有有效存储键
     * 
     * 遍历持久化数据中的所有键，返回与指定类型匹配的频率列表。
     * 主要用于命令系统和管理工具。
     * 
     * @param identifer 存储类型标识符
     * @return 该类型下所有存在数据的频率字符串列表
     */
    public List<String> getValidKeys(String identifer) {
        List<String> list = new ArrayList<>();
        for (String key : saveTag.getAllKeys()) {
            if (key.endsWith(",type=" + identifer)) {
                // 移除类型后缀，只保留频率部分
                list.add(key.replace(",type=" + identifer, ""));
            }
        }
        return list;
    }

    /**
     * 请求保存指定的存储实例
     * 
     * 将指定的存储实例添加到脏数据列表中，等待下一次保存操作时
     * 将其数据写入磁盘。这是一个延迟写入机制，可以提高性能。
     * 
     * @param storage 需要保存的存储实例
     */
    public void requestSave(AbstractEnderStorage storage) {
        dirtyStorage.add(storage);
    }
}
