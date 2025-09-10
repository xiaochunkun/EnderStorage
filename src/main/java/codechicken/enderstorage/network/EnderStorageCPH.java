package codechicken.enderstorage.network;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.enderstorage.tile.TileEnderTank;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static codechicken.enderstorage.network.EnderStorageNetwork.*;

/**
 * EnderStorage客户端数据包处理器
 * 
 * 负责处理服务器发送到客户端的所有网络数据包，实现以下功能：
 * - 方块实体状态同步：更新客户端的方块实体数据
 * - 存储容器开启状态同步：通知客户端存储容器的使用情况
 * - 液体罐数据同步：保持客户端液体罐状态与服务器一致
 * - 压力状态同步：同步液体罐的压力信息
 * 
 * 处理流程：
 * 1. 接收服务器发送的数据包
 * 2. 根据数据包类型分发到对应的处理逻辑
 * 3. 更新客户端相应的游戏对象状态
 * 4. 确保客户端显示与服务器状态保持同步
 * 
 * @author EnderStorage Team
 * @since 1.0.0
 */
public class EnderStorageCPH implements IClientPacketHandler {

    /**
     * 处理服务器发送的数据包
     * 
     * 根据数据包类型执行相应的客户端同步操作：
     * - C_TILE_UPDATE: 同步方块实体数据（频率、状态等）
     * - C_SET_CLIENT_OPEN: 更新存储容器的客户端开启状态
     * - C_TANK_SYNC: 同步液体罐内容到客户端缓存
     * - C_LIQUID_SYNC: 同步特定液体罐方块实体的液体状态
     * - C_PRESSURE_SYNC: 同步液体罐的压力状态
     * 
     * @param packet 来自服务器的数据包，包含同步数据
     * @param mc Minecraft客户端实例，用于访问客户端世界和实体
     */
    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc) {
        assert mc.level != null; // 确保客户端世界已加载
        switch (packet.getType()) {
            case C_TILE_UPDATE:
                // 处理方块实体更新：读取位置并更新对应的频率拥有者方块实体
                if (mc.level.getBlockEntity(packet.readPos()) instanceof TileFrequencyOwner tile) {
                    tile.readFromPacket(packet);
                }
                break;
            case C_SET_CLIENT_OPEN:
                // 处理客户端开启状态：更新指定频率存储的客户端开启计数
                EnderStorageManager.instance(true).getStorage(Frequency.readFromPacket(packet), EnderItemStorage.TYPE).setClientOpen(packet.readBoolean() ? 1 : 0);
                break;
            case C_TANK_SYNC:
                // 处理液体罐同步：通过同步器更新客户端液体罐状态
                TankSynchroniser.syncClient(Frequency.readFromPacket(packet), packet.readFluidStack());
                break;
            case C_LIQUID_SYNC:
                // 处理液体状态同步：更新特定位置液体罐的液体状态
                if (mc.level.getBlockEntity(packet.readPos()) instanceof TileEnderTank tile) {
                    tile.liquid_state.sync(packet.readFluidStack());
                }
                break;
            case C_PRESSURE_SYNC:
                // 处理压力同步：更新液体罐的压力状态
                if (mc.level.getBlockEntity(packet.readPos()) instanceof TileEnderTank tile) {
                    tile.pressure_state.a_pressure = packet.readBoolean();
                }
                break;
        }
    }
}
