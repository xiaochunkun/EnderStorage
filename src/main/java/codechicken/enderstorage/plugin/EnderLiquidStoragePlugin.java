package codechicken.enderstorage.plugin;

import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderLiquidStorage;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * 末影液体存储插件类
 * 
 * 实现末影液体存储系统的插件接口，负责管理所有末影罐的液体存储。
 * 这个插件处理液体存储的创建、标识符提供和客户端同步。
 * 
 * 主要功能：
 * - 创建末影液体存储实例
 * - 提供存储类型标识符
 * - 处理客户端信息同步（目前为空实现）
 * 
 * 注意：液体存储的客户端同步目前是空实现，因为液体存储不需要
 * 像物品存储那样的开关状态同步。
 * 
 * @author EnderStorage团队
 */
public class EnderLiquidStoragePlugin implements EnderStoragePlugin<EnderLiquidStorage> {

    /**
     * 创建末影液体存储实例
     * 
     * 根据给定的管理器和频率创建一个新的末影液体存储。
     * 这个方法在需要创建新存储实例时被调用。
     * 
     * @param manager 末影存储管理器
     * @param freq 存储频率
     * @return 新的末影液体存储实例
     */
    @Override
    public EnderLiquidStorage createEnderStorage(EnderStorageManager manager, Frequency freq) {
        return new EnderLiquidStorage(manager, freq);
    }

    /**
     * 获取存储类型标识符
     * 
     * 返回这个插件管理的存储类型标识符。
     * 用于区分不同类型的末影存储系统。
     * 
     * @return 末影液体存储类型
     */
    @Override
    public StorageType<EnderLiquidStorage> identifier() {
        return EnderLiquidStorage.TYPE;
    }

    /**
     * 向客户端发送存储信息
     * 
     * 对于液体存储，这是一个空实现。
     * 液体存储不需要像物品存储那样的开关状态同步，
     * 因为液体的视觉效果和状态管理不同。
     * 
     * @param player 目标玩家
     * @param list 需要同步的存储列表（未使用）
     */
    @Override
    public void sendClientInfo(ServerPlayer player, List<EnderLiquidStorage> list) {
        // 液体存储不需要客户端同步，所以这里为空实现
    }
}
