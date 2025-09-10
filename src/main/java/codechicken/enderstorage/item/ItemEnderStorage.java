package codechicken.enderstorage.item;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * EnderStorage方块物品基类
 * 
 * 为EnderStorage系列方块提供物品形态的基础功能，包括：
 * - 频率信息存储：在物品NBT中保存和读取频率数据
 * - 方块放置逻辑：放置时自动设置方块实体的频率
 * - 工具提示显示：显示频率信息和拥有者名称
 * - 扩展能力支持：为特定类型提供额外的能力接口
 * 
 * 继承体系：
 * - 继承BlockItem：提供标准的方块物品功能
 * - 被具体类继承：如EnderChest、EnderTank等物品类
 * 
 * 核心特性：
 * - 频率持久化：频率信息与物品绑定，支持跨世界传输
 * - 无缝集成：与原版方块物品系统完美兼容
 * - 信息透明：通过工具提示向玩家展示频率状态
 * 
 * @author EnderStorage Team
 * @since 1.0.0
 */
public class ItemEnderStorage extends BlockItem {

    /**
     * 构造函数
     * 
     * 创建与指定方块关联的EnderStorage物品。
     * 
     * @param block 关联的EnderStorage方块
     */
    public ItemEnderStorage(Block block) {
        super(block, new Properties());
    }

    /**
     * 从物品堆中获取频率信息
     * 
     * 读取存储在物品NBT中的频率数据。
     * 如果物品没有频率信息，则返回默认频率。
     * 
     * @param stack 物品堆
     * @return 频率信息
     */
    public Frequency getFreq(ItemStack stack) {
        return Frequency.readFromStack(stack);
    }

    /**
     * 更新方块实体标签（重写）
     * 
     * 在方块放置后，将物品中存储的频率信息设置到方块实体。
     * 这确保了频率信息从物品形态正确传递到方块形态。
     * 
     * @param pos 方块位置
     * @param world 世界实例
     * @param player 放置方块的玩家（可为null）
     * @param stack 物品堆
     * @param state 方块状态
     * @return true表示成功更新，false表示更新失败
     */
    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean flag = super.updateCustomBlockEntityTag(pos, world, player, stack, state);
        
        // 获取方块实体并设置频率
        TileFrequencyOwner tile = (TileFrequencyOwner) world.getBlockEntity(pos);
        if (tile != null) {
            tile.setFreq(getFreq(stack));
            return true;
        }

        return flag;
    }

    /**
     * 添加物品工具提示信息
     * 
     * 在物品的工具提示中显示：
     * - 频率拥有者名称（如果有）
     * - 频率的详细信息（颜色组合等）
     * 
     * @param stack 物品堆
     * @param ctx 工具提示上下文
     * @param tooltip 工具提示列表
     * @param flagIn 工具提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flagIn) {
        Frequency frequency = Frequency.readFromStack(stack);
        
        // 添加拥有者名称（如果存在）
        frequency.ownerName().ifPresent(tooltip::add);
        
        // 添加频率信息
        tooltip.add(frequency.getTooltip());
    }

    //    private EnderLiquidStorage getLiquidStorage(ItemStack stack) {
    //        return (EnderLiquidStorage) EnderStorageManager.instance(FMLCommonHandler.instance().getSide().isClient()).getStorage(getFreq(stack), "liquid");
    //    }
    //
    //    @Override
    //    public ICapabilityProvider initCapabilities(final ItemStack stack, NBTTagCompound nbt) {
    //        if (getMetadata(stack) == 1) {
    //            return new ICapabilityProvider() {
    //                @Override
    //                public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
    //
    //                    return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    //                }
    //
    //                @Override
    //                public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
    //
    //                    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? getLiquidStorage(stack) : null);
    //                }
    //            };
    //        }
    //        return null;
    //    }
}
