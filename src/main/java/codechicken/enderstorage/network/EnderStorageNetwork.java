package codechicken.enderstorage.network;

import codechicken.enderstorage.EnderStorage;
import codechicken.lib.packet.PacketCustomChannel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

import static codechicken.enderstorage.EnderStorage.MOD_ID;

/**
 * EnderStorage网络通信管理器
 * 
 * 负责管理EnderStorage模组的网络通信系统，包括：
 * - 客户端与服务器之间的数据包传输通道
 * - 网络协议版本管理和兼容性检查
 * - 不同类型数据包的标识符定义
 * - 网络处理器的注册和初始化
 * 
 * 网络架构特点：
 * - 使用自定义通道进行通信，避免与其他模组冲突
 * - 支持版本化协议，确保客户端和服务器版本匹配
 * - 分离客户端和服务器处理器，提高代码组织性
 * - 定义标准化的数据包类型常量
 * 
 * Created by covers1624 on 28/10/19.
 * 
 * @author covers1624
 * @since 1.0.0
 */
public class EnderStorageNetwork {

    /** 网络通道资源位置标识符，用于标识EnderStorage模组的网络通道 */
    public static final ResourceLocation NET_CHANNEL = ResourceLocation.fromNamespaceAndPath(MOD_ID, "network");
    
    /** 
     * 自定义数据包通道实例
     * 
     * 配置特点：
     * - 版本化支持：使用模组版本进行协议兼容性检查
     * - 客户端处理器：EnderStorageCPH负责处理服务器发送的数据包
     * - 服务器处理器：EnderStorageSPH负责处理客户端发送的数据包
     */
    public static final PacketCustomChannel channel = new PacketCustomChannel(NET_CHANNEL)
            .versioned(EnderStorage.container().getModInfo().getVersion().toString())
            .client(() -> EnderStorageCPH::new)
            .server(() -> EnderStorageSPH::new);

    // ==================== 客户端处理的数据包类型 ====================
    
    /** 方块实体更新数据包 - 同步方块实体状态到客户端 */
    public static final int C_TILE_UPDATE = 1;
    
    /** 客户端开启状态数据包 - 通知客户端存储容器的开启状态 */
    public static final int C_SET_CLIENT_OPEN = 2;
    
    /** 液体罐同步数据包 - 同步液体罐内容到客户端 */
    public static final int C_TANK_SYNC = 3;
    
    /** 液体状态同步数据包 - 同步特定液体罐的液体状态 */
    public static final int C_LIQUID_SYNC = 4;
    
    /** 压力同步数据包 - 同步液体罐的压力状态 */
    public static final int C_PRESSURE_SYNC = 5;

    // ==================== 服务器处理的数据包类型 ====================
    
    /** 可见性控制数据包 - 客户端请求特定频率存储的可见性控制 */
    public static final int S_VISIBILITY = 1;

    /**
     * 初始化网络通信系统
     * 
     * 将网络通道注册到模组事件总线，启用网络功能。
     * 必须在模组初始化阶段调用此方法以确保网络通信正常工作。
     * 
     * @param modBus 模组事件总线，用于注册网络事件监听器
     */
    public static void init(IEventBus modBus) {
        channel.init(modBus);
    }

}
