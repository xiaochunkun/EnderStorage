package codechicken.enderstorage;

import codechicken.enderstorage.config.EnderStorageConfig;
import codechicken.enderstorage.init.ClientInit;
import codechicken.enderstorage.init.DataGenerators;
import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.network.EnderStorageNetwork;
import codechicken.enderstorage.network.TankSynchroniser;
import codechicken.enderstorage.plugin.EnderItemStoragePlugin;
import codechicken.enderstorage.plugin.EnderLiquidStoragePlugin;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import static codechicken.enderstorage.EnderStorage.MOD_ID;
import static java.util.Objects.requireNonNull;

/**
 * EnderStorage末影存储模组主类
 * 
 * 这是末影存储模组的核心入口类，负责整个模组的初始化流程。
 * 末影存储模组提供了末影箱子和末影储罐等跨维度共享存储功能，
 * 允许玩家通过颜色频率系统访问共享的物品和液体存储空间。
 * 
 * 主要功能：
 * - 模组组件的初始化和注册
 * - 配置文件的加载
 * - 客户端/服务端环境的区分处理
 * - 存储管理器和插件系统的初始化
 * - 网络通信和数据同步的设置
 * 
 * @author CodeChickenCore团队
 * @since 1.0.0
 */
@Mod (MOD_ID)
public class EnderStorage {

    /** 模组标识符，用于Minecraft识别此模组 */
    public static final String MOD_ID = "enderstorage";

    /** 模组容器实例，用于访问模组相关信息 */
    private static @Nullable ModContainer container;

    /**
     * 模组构造函数 - 模组的主要初始化入口点
     * 
     * 此方法在模组加载时被NeoForge框架调用，负责完整的模组初始化流程：
     * 1. 加载配置文件
     * 2. 初始化模组内容（方块、物品、瓦片实体等）
     * 3. 根据运行环境初始化客户端特有功能
     * 4. 设置网络通信系统
     * 5. 注册存储插件和事件处理器
     * 6. 初始化数据生成器
     * 
     * @param container 模组容器，提供模组元数据和配置访问
     * @param modBus 模组事件总线，用于注册模组相关事件监听器
     */
    public EnderStorage(ModContainer container, IEventBus modBus) {
        // 保存模组容器引用
        EnderStorage.container = container;
        
        // 加载模组配置文件
        EnderStorageConfig.load();

        // 初始化模组内容（方块、物品、瓦片实体等）
        EnderStorageModContent.init(modBus);
        
        // 仅在客户端环境下初始化客户端特有功能
        if (FMLEnvironment.dist.isClient()) {
            ClientInit.init(modBus);
        }

        // 初始化网络通信系统
        EnderStorageNetwork.init(modBus);

        // 初始化存储管理器
        EnderStorageManager.init();
        // 注册物品存储插件
        EnderStorageManager.registerPlugin(new EnderItemStoragePlugin());
        // 注册液体存储插件
        EnderStorageManager.registerPlugin(new EnderLiquidStoragePlugin());

        // 注册存储数据保存事件处理器
        NeoForge.EVENT_BUS.register(new EnderStorageManager.EnderStorageSaveHandler());
        // 注册储罐同步处理器
        NeoForge.EVENT_BUS.register(new TankSynchroniser());

        // 初始化数据生成器（用于生成合成配方、标签等）
        DataGenerators.init(modBus);
    }

    /**
     * 获取模组容器实例
     * 
     * @return 模组容器实例，包含模组的元数据信息
     * @throws NullPointerException 如果容器未初始化
     */
    public static ModContainer container() {
        return requireNonNull(container);
    }

    //    @Mod.EventHandler
    //    public void serverStarting(FMLServerStartingEvent event) {
    //        event.registerServerCommand(new EnderStorageCommand());
    //    }

}
