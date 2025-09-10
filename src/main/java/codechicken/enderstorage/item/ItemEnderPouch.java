package codechicken.enderstorage.item;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.config.EnderStorageConfig;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.enderstorage.tile.TileEnderChest;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * EnderPouch（末影袋）物品类
 * 
 * 提供便携式访问EnderChest存储的物品，主要功能包括：
 * - 便携访问：无需放置方块即可访问对应频率的存储空间
 * - 频率复制：可以从现有的EnderChest复制频率设置
 * - 潜行交互：通过潜行+右键从EnderChest复制频率
 * - 存储界面：右键直接打开对应频率的存储界面
 * 
 * 使用方式：
 * - 普通右键：打开对应频率的存储界面
 * - 潜行+右键方块：从EnderChest复制频率到袋子
 * - 工具提示：显示当前绑定的频率信息
 * 
 * 设计特点：
 * - 单一堆叠：每个袋子只能堆叠1个，保持频率独立性
 * - 权限控制：支持无政府模式下的权限管理
 * - 无缝集成：与EnderChest系统完美兼容
 * 
 * @author EnderStorage Team
 * @since 1.0.0
 */
public class ItemEnderPouch extends Item {

    /**
     * 构造函数
     * 
     * 创建EnderPouch物品，设置为不可堆叠以保持频率独立性。
     */
    public ItemEnderPouch() {
        super(new Item.Properties()
                .stacksTo(1) // 最大堆叠数为1，保持频率独立性
        );
    }

    /**
     * 添加物品工具提示信息
     * 
     * 显示当前绑定的频率信息，包括拥有者名称和颜色组合。
     * 
     * @param stack 物品堆
     * @param ctx 工具提示上下文
     * @param tooltip 工具提示列表
     * @param flagIn 工具提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flagIn) {
        Frequency frequency = Frequency.readFromStack(stack);
        frequency.ownerName().ifPresent(tooltip::add); // 添加拥有者名称
        tooltip.add(frequency.getTooltip());           // 添加频率信息
    }

    /**
     * 物品优先使用处理（针对方块）
     * 
     * 处理玩家潜行+右键EnderChest的频率复制功能。
     * 只在服务器端执行，并考虑无政府模式下的权限控制。
     * 
     * @param stack 物品堆
     * @param context 使用上下文（包含玩家、位置等信息）
     * @return 交互结果：SUCCESS表示成功复制，PASS表示不处理
     */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) {
            return InteractionResult.PASS; // 客户端不处理
        }

        Player player = context.getPlayer();
        BlockEntity tile = world.getBlockEntity(context.getClickedPos());
        
        // 检查是否为潜行+右键EnderChest
        if (tile instanceof TileEnderChest chest && player != null && player.isCrouching()) {
            Frequency frequency = chest.getFrequency();
            
            // 在无政府模式下，如果不是拥有者，则移除拥有者信息
            if (EnderStorageConfig.anarchyMode && !(frequency.owner().isPresent() && frequency.owner().get().equals(player.getUUID()))) {
                frequency = frequency.withoutOwner();
            }

            // 将频率信息写入物品
            frequency.writeToStack(stack);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 物品使用处理（空气中右键）
     * 
     * 处理玩家右键EnderPouch打开存储界面的功能。
     * 潜行状态下不打开界面，以防止与频率复制功能冲突。
     * 
     * @param world 世界实例
     * @param player 使用物品的玩家
     * @param hand 使用的手（主手或副手）
     * @return 交互结果和更新后的物品堆
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 潜行状态下不打开界面，留给频率复制功能
        if (player.isCrouching()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        
        // 只在服务器端打开界面
        if (!world.isClientSide) {
            Frequency frequency = Frequency.readFromStack(stack);
            // 打开对应频率的存储界面
            EnderStorageManager.instance(false).getStorage(frequency, EnderItemStorage.TYPE)
                .openContainer((ServerPlayer) player, Component.translatable(stack.getDescriptionId()));
        }
        
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
