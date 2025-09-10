package codechicken.enderstorage.network;

import codechicken.enderstorage.api.Frequency;
import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import static codechicken.enderstorage.network.EnderStorageNetwork.S_VISIBILITY;

/**
 * EnderStorage服务器数据包处理器
 * 
 * 负责处理客户端发送到服务器的网络数据包，主要功能包括：
 * - 处理客户端的可见性请求：管理客户端对特定频率存储的可见性控制
 * - 发送开启状态更新：通知客户端存储容器的开启状态变化
 * 
 * 设计特点：
 * - 轻量级处理器：仅处理关键的服务器端逻辑
 * - 安全验证：确保只有合法的客户端请求被处理
 * - 状态同步：及时将服务器状态变化推送给客户端
 * 
 * @author EnderStorage Team  
 * @since 1.0.0
 */
public class EnderStorageSPH implements IServerPacketHandler {

    /**
     * 处理客户端发送的数据包
     * 
     * 目前主要处理可见性控制请求，客户端通过此机制告知服务器
     * 需要同步哪些频率的液体罐状态，服务器据此优化网络传输。
     * 
     * @param packet 来自客户端的数据包
     * @param sender 发送数据包的玩家，用于验证权限和回传数据
     */
    @Override
    public void handlePacket(PacketCustom packet, ServerPlayer sender) {
        switch (packet.getType()) {
            case S_VISIBILITY:
                // 处理可见性控制：委托给液体罐同步器处理客户端的可见性请求
                TankSynchroniser.handleVisiblityPacket(sender, packet);
                break;
        }
    }

    /**
     * 发送存储容器开启状态更新到客户端
     * 
     * 当服务器上的存储容器开启状态发生变化时，通过此方法
     * 通知对应的客户端更新其本地状态，确保UI显示正确。
     * 
     * @param player 目标玩家，如果为null则发送给所有在线玩家
     * @param freq 存储容器的频率标识
     * @param open 开启状态：true表示已开启，false表示已关闭
     */
    public static void sendOpenUpdateTo(@Nullable ServerPlayer player, Frequency freq, boolean open) {
        // 创建开启状态更新数据包
        PacketCustom packet = new PacketCustom(EnderStorageNetwork.NET_CHANNEL, EnderStorageNetwork.C_SET_CLIENT_OPEN, ServerLifecycleHooks.getCurrentServer().registryAccess());
        freq.writeToPacket(packet); // 写入频率数据
        packet.writeBoolean(open);   // 写入开启状态
        packet.sendToPlayer(player); // 发送给指定玩家
    }
}
