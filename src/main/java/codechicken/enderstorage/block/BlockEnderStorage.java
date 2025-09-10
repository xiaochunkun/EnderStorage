package codechicken.enderstorage.block;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.config.EnderStorageConfig;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.lib.util.ItemUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象末影存储方块基类
 * 
 * 这是所有末影存储方块（末影箱子、末影储罐等）的基础类，提供了共同的行为和交互逻辑。
 * 该类继承自BaseEntityBlock，具有瓦片实体功能，支持数据存储和同步。
 * 
 * 核心功能：
 * - 频率颜色调整：玩家可以使用染料右键调整方块的颜色频率
 * - 私人/公共模式：使用个人物品可以设置私人频率
 * - 红石交互：支持比较器输出和红石信号
 * - 自定义渲染：使用INVISIBLE渲染形状，由自定义渲染器处理
 * - 破坏保护：确保方块被破坏时正确掉落物品
 * 
 * 交互逻辑：
 * - subHit 1-3：颜色按钮，用染料右键可以调整对应位置的颜色
 * - subHit 4：个人按钮，用于设置或移除私人模式
 * - 其他区域：打开存储界面或执行特定操作
 * 
 * @author covers1624
 * @since 4/11/2016
 */
public abstract class BlockEnderStorage extends BaseEntityBlock {

    /**
     * 构造函数
     * 
     * @param properties 方块属性配置
     */
    public BlockEnderStorage(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 获取渲染形状
     * 
     * 返回INVISIBLE表示此方块不使用标准的方块模型渲染，
     * 而是由自定义的瓦片实体渲染器（TESR）处理渲染逻辑。
     * 
     * @param state 方块状态
     * @return 渲染形状枚举值
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * 当方块被玩家破坏时的处理
     * 
     * 重写此方法以确保在收获模式下方块不会立即被移除，
     * 允许playerDestroy方法正确处理掉落物品的逻辑。
     * 
     * @param state 方块状态
     * @param world 世界对象
     * @param pos 方块位置
     * @param player 破坏方块的玩家
     * @param willHarvest 是否会收获（掉落物品）
     * @param fluid 流体状态
     * @return 是否允许移除方块
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return willHarvest || super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    /**
     * 玩家破坏方块后的处理
     * 
     * 在调用父类方法处理掉落物品后，确保方块位置被设置为空气。
     * 这是必要的，因为onDestroyedByPlayer可能延迟了方块的移除。
     * 
     * @param worldIn 世界对象
     * @param player 破坏方块的玩家
     * @param pos 方块位置
     * @param state 方块状态
     * @param te 瓦片实体（可能为null）
     * @param stack 用于破坏的工具
     */
    @Override
    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, state, te, stack);
        worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    /**
     * 获取方块被破坏时的掉落物品
     * 
     * 根据瓦片实体中存储的频率信息创建对应的物品。
     * 如果启用了无政府模式且存储有拥有者，还会额外掉落个人物品。
     * 
     * @param state 方块状态
     * @param builder 战利品参数构建器
     * @return 掉落的物品列表
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        TileFrequencyOwner tile = (TileFrequencyOwner) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (tile != null) {
            // 创建包含频率信息的物品
            drops.add(createItem(tile.getFrequency()));
            // 在无政府模式下，私人频率会额外掉落个人物品
            if (EnderStorageConfig.anarchyMode && tile.getFrequency().hasOwner()) {
                drops.add(EnderStorageConfig.getPersonalItem().copy());
            }
        }
        return drops;
    }

    /**
     * 获取创意模式中重复的方块物品
     * 
     * 当玩家在创意模式中使用中键复制方块时，
     * 返回包含当前频率设置的物品堆叠。
     * 
     * @param state 方块状态
     * @param rayTraceResult 射线追踪结果
     * @param world 世界访问器
     * @param pos 方块位置
     * @param player 玩家对象
     * @return 包含频率信息的物品堆叠
     */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult rayTraceResult, LevelReader world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof TileFrequencyOwner tile) {
            return createItem(tile.getFrequency());
        }
        return ItemStack.EMPTY;
    }

    /**
     * 创建包含指定频率的物品
     * 
     * 根据给定的频率信息创建对应的物品堆叠。
     * 在无政府模式下，会移除频率的拥有者信息。
     * 
     * @param freq 要嵌入物品的频率信息
     * @return 包含频率数据的物品堆叠
     */
    private ItemStack createItem(Frequency freq) {
        // 在无政府模式下移除拥有者信息
        if (EnderStorageConfig.anarchyMode) {
            freq = freq.withoutOwner();
        }
        ItemStack stack = new ItemStack(this, 1);
        freq.writeToStack(stack);
        return stack;
    }

    /**
     * 处理玩家使用物品右键方块的交互
     * 
     * 这是末影存储方块最复杂的交互逻辑，处理各种操作：
     * 
     * 交互区域说明：
     * - subHit 1-3: 颜色按钮区域，可用染料调整对应位置的颜色
     * - subHit 4: 个人按钮区域，用于设置或移除私人模式
     * - 其他: 主体区域，打开存储界面或执行特定操作
     * 
     * 操作逻辑：
     * 1. 个人按钮 + Shift：移除私人模式，掉落个人物品
     * 2. 个人按钮 + 个人物品：设置私人模式
     * 3. 颜色按钮 + 染料：调整对应位置的颜色
     * 4. 其他区域 + 非shiftshift：激活存储（打开GUI或执行操作）
     * 
     * @param stack 玩家持有的物品
     * @param state 方块状态
     * @param world 世界对象
     * @param pos 方块位置
     * @param player 交互的玩家
     * @param hand 使用的手（主手/副手）
     * @param clientHit 客户端的射线追踪结果
     * @return 交互结果，指示操作是否成功
     */
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult clientHit) {
        // 客户端直接返回成功，由服务端处理实际逻辑
        if (world.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        
        BlockEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof TileFrequencyOwner owner)) {
            return ItemInteractionResult.FAIL;
        }

        // 使用高精度射线追踪确定点击的具体区域
        HitResult rawHit = RayTracer.retrace(player);
        if (!(rawHit instanceof SubHitBlockHitResult hit)) {
            return ItemInteractionResult.FAIL;
        }
        
        // 处理个人按钮区域的交互（subHit == 4）
        if (hit.subHit == 4) {
            ItemStack item = player.getInventory().getSelected();
            
            // Shift + 右键：移除私人模式
            if (player.isCrouching() && owner.getFrequency().hasOwner()) {
                // 在非创意模式下，检查能否给玩家个人物品
                if (!player.getAbilities().instabuild && !player.getInventory().add(EnderStorageConfig.getPersonalItem().copy())) {
                    return ItemInteractionResult.FAIL;
                }

                // 移除拥有者，转为公共频率
                owner.setFreq(owner.getFrequency().withoutOwner());
                return ItemInteractionResult.SUCCESS;
                
            // 持有个人物品右键：设置私人模式
            } else if (!item.isEmpty() && ItemUtils.areStacksSameType(item, EnderStorageConfig.getPersonalItem())) {
                if (!owner.getFrequency().hasOwner()) {
                    // 设置当前玩家为拥有者
                    owner.setFreq(owner.getFrequency().withOwner(player));
                    // 在非创意模式下消耗物品
                    if (!player.getAbilities().instabuild) {
                        item.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            
        // 处理颜色按钮区域的交互（subHit 1-3）
        } else if (hit.subHit >= 1 && hit.subHit <= 3) {
            ItemStack item = player.getInventory().getSelected();
            if (!item.isEmpty()) {
                // 检查是否为染料物品
                EnumColour dye = EnumColour.fromDyeStack(item);
                if (dye != null) {
                    // 创建颜色数组，只修改对应位置的颜色
                    EnumColour[] colours = { null, null, null };
                    if (colours[hit.subHit - 1] == dye) {
                        return ItemInteractionResult.FAIL; // 颜色相同，不需要修改
                    }
                    colours[hit.subHit - 1] = dye;
                    
                    // 更新频率的指定颜色
                    owner.setFreq(owner.getFrequency().withColours(colours));
                    // 在非创意模式下消耗染料
                    if (!player.getAbilities().instabuild) {
                        item.shrink(1);
                    }
                    return ItemInteractionResult.FAIL; // 返回FAIL避免额外的交互处理
                }
            }
        }
        
        // 处理主体区域的交互：在非Shift模式下激活存储
        return !player.isCrouching() && owner.activate(player, hit.subHit, hand) 
               ? ItemInteractionResult.SUCCESS 
               : ItemInteractionResult.FAIL;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (world.getBlockEntity(pos) instanceof TileFrequencyOwner tile) {
            tile.onPlaced(placer);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFrequencyOwner) {
            return ((TileFrequencyOwner) tile).getLightValue();
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        BlockEntity tile = world.getBlockEntity(pos);
        return tile instanceof TileFrequencyOwner && ((TileFrequencyOwner) tile).redstoneInteraction();
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        return tile instanceof TileFrequencyOwner ? ((TileFrequencyOwner) tile).comparatorOutput() : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation direction) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFrequencyOwner) {
            ((TileFrequencyOwner) tile).rotate();
        }
        return state;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, worldIn, pos, eventID, eventParam);
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity != null && tileentity.triggerEvent(eventID, eventParam);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        throw new UnsupportedOperationException();
    }
}
