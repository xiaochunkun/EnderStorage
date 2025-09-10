package codechicken.enderstorage.block;

import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.enderstorage.tile.TileEnderTank;
import codechicken.enderstorage.tile.TileFrequencyOwner;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static codechicken.lib.vec.Vector3.CENTER;

/**
 * 末影储罐方块类 - 液体存储的物理方块实现
 * 
 * <p>末影储罐是末影存储系统中用于液体存储的核心方块。它提供了跨维度的液体存储功能，
 * 具有复杂的交互界面和精确的碰撞检测系统。储罐具有圆柱形的主体和可交互的按钮界面，
 * 支持频率调整和视觉状态显示。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li><strong>液体存储</strong>：提供16桶容量的跨维度液体存储</li>
 *   <li><strong>精确交互</strong>：支持对按钮、表盘等不同区域的独立交互</li>
 *   <li><strong>动态形状</strong>：根据方块方向提供准确的碰撞和选择框</li>
 *   <li><strong>视觉反馈</strong>：储罐液面高度实时反映存储状态</li>
 * </ul>
 * 
 * <h3>物理结构：</h3>
 * <ul>
 *   <li><strong>主体储罐</strong>：圆柱形容器，占据方块的主要空间</li>
 *   <li><strong>控制按钮</strong>：3个颜色调整按钮，支持频率设置</li>
 *   <li><strong>信息表盘</strong>：显示当前频率和所有权信息</li>
 *   <li><strong>旋转支持</strong>：支持4个方向的摆放，界面相应调整</li>
 * </ul>
 * 
 * <h3>交互系统：</h3>
 * <p>使用IndexedVoxelShape系统实现精确的区域交互：</p>
 * <ul>
 *   <li>索引0：主储罐体 - 右键打开液体界面</li>
 *   <li>索引1-3：颜色按钮 - 调整频率颜色</li>
 *   <li>索引4：信息表盘 - 显示状态和权限信息</li>
 * </ul>
 * 
 * <h3>技术实现：</h3>
 * <ul>
 *   <li>静态预计算所有方向的VoxelShape，提高运行时性能</li>
 *   <li>支持BlockEntityTicker，启用液体同步和状态更新</li>
 *   <li>使用Transformation系统处理方向旋转</li>
 * </ul>
 * 
 * @author covers1624, ChickenBones
 * @since 1.0.0
 * @see BlockEnderStorage
 * @see TileEnderTank
 */
public class BlockEnderTank extends BlockEnderStorage {

    /** 主储罐的VoxelShape - 圆柱形容器的碰撞和选择框 */
    private static final IndexedVoxelShape TANK = new IndexedVoxelShape(Shapes.create(0.15, 0, 0.15, 0.85, 0.916, 0.85), 0);
    
    /** 控制按钮的VoxelShape数组 - [方向][按钮索引] */
    private static final IndexedVoxelShape[][] BUTTONS = new IndexedVoxelShape[4][3];
    
    /** 信息表盘的VoxelShape数组 - 按方向索引 */
    private static final IndexedVoxelShape[] DIAL = new IndexedVoxelShape[4];
    
    /** 完整的组合VoxelShape - 包含所有可交互区域 */
    private static final MultiIndexedVoxelShape[] SHAPES = new MultiIndexedVoxelShape[4];

    /** 按钮变换矩阵 - 用于计算按钮在不同位置的坐标变换 */
    public static Transformation[] buttonT = new Transformation[3];

    /**
     * 静态初始化块 - 预计算所有方向的VoxelShape
     * 
     * <p>在类加载时计算所有方向（北、东、南、西）的按钮、表盘和组合形状，
     * 避免运行时重复计算，提高游戏性能。</p>
     */
    static {
        // 初始化3个按钮的位置变换矩阵
        for (int i = 0; i < 3; i++) {
            buttonT[i] = new Scale(0.6).with(new Translation(0.35 + (2 - i) * 0.15, 0.91, 0.5));
        }

        // 表盘的基础立方体定义
        Cuboid6 dialBase = new Cuboid6(0.358, 0.268, 0.05, 0.662, 0.565, 0.15);
        
        // 为每个旋转方向计算VoxelShape
        for (int rot = 0; rot < 4; rot++) {
            Transformation rotation = Rotation.quarterRotations[rot ^ 2].at(CENTER);
            
            // 计算当前方向下的3个按钮形状
            for (int button = 0; button < 3; button++) {
                BUTTONS[rot][button] = new IndexedVoxelShape(
                        VoxelShapeCache.getShape(TileFrequencyOwner.SELECTION_BUTTON.copy().apply(buttonT[button]).apply(rotation)),
                        button + 1 // 索引1-3对应3个按钮
                );
            }
            
            // 计算当前方向下的表盘形状
            DIAL[rot] = new IndexedVoxelShape(VoxelShapeCache.getShape(dialBase.copy().apply(rotation)), 4);

            // 组合所有形状为完整的多索引形状
            ImmutableSet.Builder<IndexedVoxelShape> cuboids = ImmutableSet.builder();
            cuboids.add(TANK);        // 主储罐
            cuboids.add(BUTTONS[rot]); // 当前方向的按钮
            cuboids.add(DIAL[rot]);   // 当前方向的表盘
            SHAPES[rot] = new MultiIndexedVoxelShape(TANK, cuboids.build());
        }
    }

    /**
     * 构造函数
     * 
     * @param properties 方块属性，包含硬度、声音、材质等信息
     */
    public BlockEnderTank(Properties properties) {
        super(properties);
    }

    /**
     * 获取方块的碰撞和选择形状
     * 
     * <p>根据储罐的旋转方向返回对应的组合VoxelShape，确保所有交互区域
     * （主储罐、按钮、表盘）都能被正确检测和交互。</p>
     * 
     * @param state 方块状态
     * @param worldIn 世界读取器
     * @param pos 方块位置
     * @param context 碰撞上下文
     * @return 对应方向的完整VoxelShape
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        VoxelShape shape = TANK; // 默认只使用储罐形状
        BlockEntity t = worldIn.getBlockEntity(pos);
        if (t instanceof TileEnderTank tile) {
            // 如果方块实体加载完成，使用完整的组合形状
            shape = SHAPES[tile.rotation];
        }
        return shape;
    }

    /**
     * 创建方块实体实例
     * 
     * @param pos 方块位置
     * @param state 方块状态
     * @return 新的TileEnderTank实例
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEnderTank(pos, state);
    }

    /**
     * 获取方块实体的Ticker
     * 
     * <p>返回液体储罐的更新Ticker，用于处理液体同步、压力输出、
     * 动画更新等需要定期执行的逻辑。</p>
     * 
     * @param level 世界级别（客户端或服务器）
     * @param state 方块状态
     * @param type 方块实体类型
     * @return 对应的BlockEntityTicker
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, EnderStorageModContent.ENDER_TANK_TILE.get(), (world, pos, blockState, tile) -> tile.tick());
    }
}
