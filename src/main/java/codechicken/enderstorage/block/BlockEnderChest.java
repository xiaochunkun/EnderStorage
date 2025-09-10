package codechicken.enderstorage.block;

import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.enderstorage.misc.EnderKnobSlot;
import codechicken.enderstorage.tile.TileEnderChest;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.MultiIndexedVoxelShape;
import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 末影箱子方块类
 * 
 * 这是末影箱子方块的实现类，继承自BlockEnderStorage基类。
 * 负责处理末影箱子方块的物理特性和渲染特性。
 * 
 * 主要功能：
 * - 定义箱子和按钮的碗素形状和碰撞边界
 * - 根据箱子打开状态动态调整形状（开盖隐藏按钮）
 * - 支持四个方向的旋转，自动计算对应的形状
 * - 集成方块实体的创建和更新机制
 * 
 * 设计特点：
 * - 使用预计算的碗素形状数组，提高渲染性能
 * - 索引化碗素形状系统，支持精确的交互检测
 * - 静态初始化所有可能的形状组合，避免运时计算
 * 
 * 形状管理：
 * - CHEST：主体箱子形状（始终可见）
 * - BUTTONS：三个颜色按钮的形状（仅在关闭时可见）
 * - LATCH：锁扣形状（仅在关闭时可见）
 * 
 * @author covers1624
 * @since 29/10/19
 */
public class BlockEnderChest extends BlockEnderStorage {

    /** 主体箱子的索引化碗素形状，占据大部分空间但不完全填满方块 */
    private static final IndexedVoxelShape CHEST = new IndexedVoxelShape(VoxelShapeCache.getShape(new Cuboid6(1 / 16D, 0, 1 / 16D, 15 / 16D, 14 / 16D, 15 / 16D)), 0);
    
    /** 四个方向的三个颜色按钮的索引化碗素形状数组 [rotation][button] */
    private static final IndexedVoxelShape[][] BUTTONS = new IndexedVoxelShape[4][3];
    
    /** 四个方向的锁扣索引化碗素形状数组 [rotation] */
    private static final IndexedVoxelShape[] LATCH = new IndexedVoxelShape[4];

    /** 所有可能的碗素形状组合数组 [rotation][state] - state: 0=关闭(包含按钮), 1=打开(仅箱子) */
    private static final VoxelShape[][] SHAPES = new VoxelShape[4][2];

    /** 三个按钮的位置变换数组，用于将按钮放置在正确的位置 */
    public static final Transformation[] buttonT = new Transformation[3];

    // 静态初始化块 - 预计算所有可能的碗素形状组合
    static {
        // 初始化三个按钮的位置变换，按钮水平排列在箱子顶部
        for (int button = 0; button < 3; button++) {
            buttonT[button] = new Translation(-(3 / 16D) + ((3D / 16D) * button), 14D / 16D, 0);
        }
        
        // 为四个方向生成对应的形状
        for (int rot = 0; rot < 4; rot++) {
            // 构建按钮和锁扣的形状
            for (int button = 0; button < 3; button++) {
                // 从基础按钮形状开始
                Cuboid6 cuboid = TileFrequencyOwner.SELECTION_BUTTON.copy();
                // 应用按钮位置变换
                cuboid.apply(buttonT[button]);
                // 移动到方块中心
                cuboid.apply(new Translation(0.5, 0, 0.5));
                // 根据方向旋转
                cuboid.apply(new Rotation((-90 * (rot)) * MathHelper.torad, Vector3.Y_POS).at(new Vector3(0.5, 0, 0.5)));
                // 创建索引化碗素形状，索引从1开始（因0留给箱子）
                BUTTONS[rot][button] = new IndexedVoxelShape(VoxelShapeCache.getShape(cuboid), button + 1);
            }
            // 创建锁扣形状，索引为4
            LATCH[rot] = new IndexedVoxelShape(VoxelShapeCache.getShape(new Cuboid6(new EnderKnobSlot(rot).getSelectionBB())), 4);

            // 构建所有可能的碗素形状组合
            for (int state = 0; state < 2; state++) {
                ImmutableSet.Builder<IndexedVoxelShape> cuboids = ImmutableSet.builder();
                // 箱子主体始终存在
                cuboids.add(CHEST);
                if (state == 0) { // 关闭状态 - 包含按钮和锁扣
                    cuboids.add(BUTTONS[rot]);
                    cuboids.add(LATCH[rot]);
                }
                // 创建综合碗素形状，以箱子为主体，其他作为附加组件
                SHAPES[rot][state] = new MultiIndexedVoxelShape(CHEST, cuboids.build());
            }
        }
    }

    /**
     * 构造函数
     * 
     * 初始化末影箱子方块，传递方块属性给父类。
     * 方块属性包括硬度、抗爆性、声音等物理特性。
     * 
     * @param properties 方块的行为属性配置
     */
    public BlockEnderChest(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 获取方块的碗素形状
     * 
     * 根据箱子的方向和当前状态（打开/关闭）返回对应的碗素形状。
     * 当箱子打开时，只显示箱子主体；关闭时显示箱子、按钮和锁扣。
     * 
     * @param state 方块状态
     * @param worldIn 世界访问器
     * @param pos 方块位置
     * @param context 碰撞上下文
     * @return 对应的碗素形状
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        // 默认使用箱子主体形状
        VoxelShape shape = CHEST;
        BlockEntity t = worldIn.getBlockEntity(pos);
        if (t instanceof TileEnderChest tile) {
            // 根据箱子的方向和开盖角度选择对应的形状
            // 开盖角度 >= 0 表示箱子关闭，< 0 表示打开
            shape = SHAPES[tile.rotation][tile.getRadianLidAngle(0) >= 0 ? 0 : 1];
        }
        return shape;
    }

    /**
     * 创建方块实体
     * 
     * 为此方块位置创建一个新的末影箱子方块实体。
     * 方块实体负责处理存储逻辑、动画效果和网络同步。
     * 
     * @param pos 方块位置
     * @param state 方块状态
     * @return 新创建的末影箱子方块实体，如果创建失败则返回null
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEnderChest(pos, state);
    }

    /**
     * 获取方块实体的更新器
     * 
     * 返回一个用于定期更新末影箱子方块实体的更新器。
     * 更新器负责处理开盖动画、网络同步等定期任务。
     * 
     * @param level 世界实例
     * @param state 方块状态
     * @param blockEntityType 方块实体类型
     * @param <T> 方块实体类型参数
     * @return 方块实体更新器，如果不需要更新则返回null
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // 使用辅助方法创建类型安全的更新器，确保只有末影箱子方块实体才会被更新
        return createTickerHelper(blockEntityType, EnderStorageModContent.ENDER_CHEST_TILE.get(), (level1, pos, state1, tile) -> tile.tick());
    }
}
