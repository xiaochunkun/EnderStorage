package codechicken.enderstorage.tile;

import codechicken.enderstorage.config.EnderStorageConfig;
import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.math.MathHelper;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.sounds.SoundEvents.*;

/**
 * 末影箱方块实体类 - 处理末影箱的所有游戏逻辑
 * 
 * <p>本类是末影箱的核心实现，负责处理箱子的开合动画、声音效果、玩家交互、
 * 物品存储访问以及红石信号输出等功能。它继承自TileFrequencyOwner，
 * 因此具备频率管理和跨维度同步的能力。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li><strong>动画系统</strong>：平滑的开合动画，支持多玩家同时访问</li>
 *   <li><strong>声音效果</strong>：可配置的开合声音，支持原版和自定义音效</li>
 *   <li><strong>玩家交互</strong>：右键打开界面，支持多人同时访问</li>
 *   <li><strong>红石输出</strong>：基于存储内容的比较器信号输出</li>
 *   <li><strong>方向性</strong>：支持4个方向的旋转摆放</li>
 * </ul>
 * 
 * <h3>技术特点：</h3>
 * <ul>
 *   <li>客户端-服务器同步的开合状态管理</li>
 *   <li>优化的物品处理器缓存机制</li>
 *   <li>基于游戏刻的高效更新系统</li>
 *   <li>完整的NBT序列化支持</li>
 * </ul>
 * 
 * <h3>动画系统说明：</h3>
 * <p>使用双缓冲动画系统（a_lidAngle和b_lidAngle）实现平滑的开合动画。
 * 动画速度为每帧0.1的线性插值，确保60fps下的流畅效果。</p>
 * 
 * @author ChickenBones
 * @since 1.0.0
 * @see TileFrequencyOwner
 * @see EnderItemStorage
 */
public class TileEnderChest extends TileFrequencyOwner {

    /** 当前帧的箱盖角度（0-1范围，1为完全打开） */
    public double a_lidAngle;
    /** 上一帧的箱盖角度，用于动画插值计算 */
    public double b_lidAngle;
    /** 当前打开此箱子的玩家数量（同步自服务器） */
    public int c_numOpen;
    /** 箱子的旋转方向（0-3，对应4个基本方向） */
    public int rotation;

    /** 物品处理器缓存，避免重复创建InvWrapper实例 */
    private @Nullable IItemHandler itemHandler;

    /**
     * 构造函数 - 创建末影箱方块实体
     * 
     * @param pos 方块位置
     * @param state 方块状态
     */
    public TileEnderChest(BlockPos pos, BlockState state) {
        super(EnderStorageModContent.ENDER_CHEST_TILE.get(), pos, state);
    }

    /**
     * 方块实体更新逻辑 - 每游戏刻调用
     * 
     * <p>处理开合动画、同步玩家数量、播放声音效果等核心逻辑。
     * 服务器端每秒检查一次打开状态，客户端每帧更新动画。</p>
     */
    @Override
    public void tick() {
        super.tick();

        assert level != null;
        // 服务器端：每秒同步一次打开状态，或当状态发生变化时立即同步
        if (!level.isClientSide && (level.getGameTime() % 20 == 0 || c_numOpen != getStorage().getNumOpen())) {
            c_numOpen = getStorage().getNumOpen();
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), 1, c_numOpen);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock()); // 更新红石信号
        }

        // 动画系统：双缓冲平滑插值
        b_lidAngle = a_lidAngle;
        a_lidAngle = MathHelper.approachLinear(a_lidAngle, c_numOpen > 0 ? 1 : 0, 0.1);

        // 声音效果：在特定角度播放开合音效
        if (b_lidAngle >= 0.5 && a_lidAngle < 0.5) {
            // 关闭音效：从半开到半闭时播放
            level.playSound(null, getBlockPos(), EnderStorageConfig.useVanillaEnderChestSounds ? ENDER_CHEST_CLOSE : CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        } else if (b_lidAngle == 0 && a_lidAngle > 0) {
            // 打开音效：从完全关闭到开始打开时播放
            level.playSound(null, getBlockPos(), EnderStorageConfig.useVanillaEnderChestSounds ? ENDER_CHEST_OPEN : CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    /**
     * 处理方块事件 - 接收服务器发送的状态同步事件
     * 
     * @param id 事件ID，1表示打开状态同步
     * @param type 事件数据，表示当前打开的玩家数量
     * @return 是否成功处理事件
     */
    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            c_numOpen = type; // 同步打开状态到客户端
            return true;
        }
        return false;
    }

    /**
     * 获取箱盖的弧度角度 - 用于渲染动画
     * 
     * <p>将线性的开合进度转换为符合物理规律的弧度角度。
     * 使用三次函数缓动，让动画更加自然。</p>
     * 
     * @param frame 帧间插值比例（0-1）
     * @return 箱盖角度（弧度，范围-π/2到0）
     */
    public double getRadianLidAngle(float frame) {
        // 在当前帧和上一帧之间插值，实现平滑动画
        double a = MathHelper.interpolate(b_lidAngle, a_lidAngle, frame);
        a = 1.0F - a;           // 反转（1=关闭，0=打开）
        a = 1.0F - a * a * a;   // 三次缓动函数，让动画更自然
        return a * 3.141593 * -0.5; // 转换为弧度（-90度到0度）
    }

    /**
     * 获取本方块关联的末影物品存储
     * 
     * @return 与当前频率绑定的末影物品存储实例
     */
    @Override
    public EnderItemStorage getStorage() {
        assert level != null;
        return EnderStorageManager.instance(level.isClientSide).getStorage(frequency, EnderItemStorage.TYPE);
    }

    /**
     * 频率设置后的回调 - 清理缓存和重新初始化
     * 
     * <p>当频率发生变化时，需要失效现有的物品处理器缓存，
     * 以确保下次访问时获取到正确的存储实例。</p>
     */
    @Override
    public void onFrequencySet() {
        invalidateCapabilities(); // 失效所有能力系统缓存
        itemHandler = null;       // 清理物品处理器缓存
    }

    /**
     * 将数据写入网络数据包 - 服务器到客户端同步
     * 
     * @param packet 网络数据包输出流
     */
    @Override
    public void writeToPacket(MCDataOutput packet) {
        super.writeToPacket(packet);
        packet.writeByte(rotation); // 同步旋转方向
    }

    /**
     * 从网络数据包读取数据 - 客户端接收服务器同步
     * 
     * @param packet 网络数据包输入流
     */
    @Override
    public void readFromPacket(MCDataInput packet) {
        super.readFromPacket(packet);
        rotation = packet.readUByte() & 3; // 读取旋转方向（保证0-3范围）
    }

    /**
     * 方块放置后的初始化 - 设置初始旋转方向
     * 
     * <p>根据放置实体的朝向自动设置箱子的旋转方向，
     * 让箱子正面朝向玩家。</p>
     * 
     * @param entity 放置方块的实体（通常是玩家）
     */
    @Override
    public void onPlaced(@Nullable LivingEntity entity) {
        assert level != null;
        // 根据实体朝向计算旋转方向（将360度分为4个区间）
        rotation = entity != null ? (int) Math.floor(entity.getYRot() * 4 / 360 + 2.5D) & 3 : 0;
        onFrequencySet(); // 初始化频率系统
        if (!level.isClientSide) {
            sendUpdatePacket(); // 同步到客户端
        }
    }

    /**
     * 保存附加数据到NBT - 持久化存储旋转状态
     * 
     * @param tag NBT标签
     * @param registries 注册表提供者
     */
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("rot", (byte) rotation); // 保存旋转方向
    }

    /**
     * 从NBT加载附加数据 - 恢复旋转状态
     * 
     * @param tag NBT标签
     * @param registries 注册表提供者
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        rotation = tag.getByte("rot") & 3; // 加载旋转方向（保证0-3范围）
    }

    /**
     * 玩家激活交互 - 右键打开末影箱界面
     * 
     * @param player 交互的玩家
     * @param subHit 子点击区域（未使用）
     * @param hand 使用的手
     * @return 是否成功处理交互
     */
    @Override
    public boolean activate(Player player, int subHit, InteractionHand hand) {
        // 为玩家打开末影存储界面，使用方块的本地化名称作为标题
        getStorage().openContainer((ServerPlayer) player, Component.translatable(getBlockState().getBlock().getDescriptionId()));
        return true;
    }

    /**
     * 手动旋转方块 - 支持使用扭下或其他工具旋转
     * 
     * @return 是否成功旋转
     */
    @Override
    public boolean rotate() {
        assert level != null;
        if (!level.isClientSide) {
            rotation = (rotation + 1) % 4; // 顺时针旋转90度
            sendUpdatePacket(); // 同步到客户端
        }
        return true;
    }

    /**
     * 获取比较器输出信号强度 - 用于红石系统
     * 
     * <p>根据存储内容的丰富程度计算红石信号强度，
     * 应用标准Minecraft的容器信号计算公式。</p>
     * 
     * @return 红石信号强度（0-15）
     */
    @Override
    public int comparatorOutput() {
        return ItemHandlerHelper.calcRedstoneFromInventory(getItemHandler());
    }

    /**
     * 获取物品处理器 - 用于管道系统和其他mod访问
     * 
     * <p>使用懒加载模式，仅在需要时创建包装器实例。
     * 当频率发生变化时，缓存会被清理以确保数据一致性。</p>
     * 
     * @return 物品处理器接口实例
     */
    public IItemHandler getItemHandler() {
        if (itemHandler == null) {
            itemHandler = new InvWrapper(getStorage()); // 将存储包装为物品处理器
        }
        return itemHandler;
    }
}
