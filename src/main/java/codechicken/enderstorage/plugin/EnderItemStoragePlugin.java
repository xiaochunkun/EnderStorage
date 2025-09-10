package codechicken.enderstorage.plugin;

import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.network.EnderStorageSPH;
import codechicken.enderstorage.storage.EnderItemStorage;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * 末影物品存储插件类
 * 
 * 实现末影物品存储系统的插件接口，负责管理所有末影箱和末影袋的物品存储。
 * 这个插件处理物品存储的创建、标识符提供和客户端同步。
 * 
 * 主要功能：
 * - 创建末影物品存储实例
 * - 提供存储类型标识符
 * - 向客户端发送存储状态信息
 * - 管理存储的打开/关闭状态同步
 * 
 * @author EnderStorage团队
 */
public class EnderItemStoragePlugin implements EnderStoragePlugin<EnderItemStorage> {

    /**
     * 创建末影物品存储实例
     * 
     * 根据给定的管理器和频率创建一个新的末影物品存储。
     * 这个方法在需要创建新存储实例时被调用。
     * 
     * @param manager 末影存储管理器
     * @param freq 存储频率
     * @return 新的末影物品存储实例
     */
    @Override
    public EnderItemStorage createEnderStorage(EnderStorageManager manager, Frequency freq) {
        return new EnderItemStorage(manager, freq);
    }

    /**
     * 获取存储类型标识符
     * 
     * 返回这个插件管理的存储类型标识符。
     * 用于区分不同类型的末影存储系统。
     * 
     * @return 末影物品存储类型
     */
    @Override
    public StorageType<EnderItemStorage> identifier() {
        return EnderItemStorage.TYPE;
    }

    /**
     * 向客户端发送存储信息
     * 
     * 将指定存储列表的状态信息同步到客户端。
     * 主要用于同步存储的打开/关闭状态，使客户端能够正确显示视觉效果。
     * 
     * @param player 目标玩家
     * @param list 需要同步的存储列表
     */
    @Override
    public void sendClientInfo(ServerPlayer player, List<EnderItemStorage> list) {
        // 遍历所有存储，对正在被打开的存储发送更新
        for (EnderItemStorage inv : list) {
            // 只对有玩家正在使用的存储发送更新
            if (inv.openCount() > 0) {
                EnderStorageSPH.sendOpenUpdateTo(player, inv.freq, true);
            }
        }
    }
}
