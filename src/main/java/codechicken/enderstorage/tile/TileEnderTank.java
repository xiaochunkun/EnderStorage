package codechicken.enderstorage.tile;

import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.network.EnderStorageNetwork;
import codechicken.enderstorage.network.TankSynchroniser;
import codechicken.enderstorage.storage.EnderLiquidStorage;
import codechicken.lib.capability.CapabilityCache;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.fluid.FluidUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * EnderStorage液体罐方块实体
 * 
 * 实现跨维度液体存储和传输功能的方块实体，主要特性包括：
 * - 频率化液体存储：通过颜色频率系统共享液体存储空间
 * - 自动液体输出：支持压力模式自动向相邻容器输出液体
 * - 网络同步：与客户端保持液体状态和压力状态同步
 * - 红石控制：支持红石信号控制压力输出模式
 * 
 * 核心功能：
 * - 液体状态管理：管理液体的存储、显示和同步
 * - 压力系统：控制液体的自动输出和红石交互
 * - 旋转支持：记录并同步方块的放置方向
 * - 能力提供：作为IFluidHandler提供液体操作接口
 * 
 * 设计特点：
 * - 继承频率拥有者基类，复用频率管理逻辑
 * - 双状态系统：分别管理液体状态和压力状态
 * - 优化的网络同步：只在必要时同步状态变化
 * 
 * @author EnderStorage Team
 * @since 1.0.0
 */
public class TileEnderTank extends TileFrequencyOwner {

    /** 方块的旋转角度（0-3），基于玩家放置时的朝向 */
    public int rotation;
    
    /** 液体状态管理器，负责液体的显示和同步 */
    public final EnderTankState liquid_state = new EnderTankState();
    
    /** 压力状态管理器，负责红石和自动输出控制 */
    public final PressureState pressure_state = new PressureState();
    
    /** 能力缓存，用于高效获取相邻方块的液体处理能力 */
    private final CapabilityCache capCache = new CapabilityCache();

    /** 液体处理器实例，懒加载创建 */
    private @Nullable IFluidHandler fluidHandler;

    /** 是否已接收到初始描述数据包（客户端） */
    private boolean described;

    /**
     * 构造函数
     * 
     * @param pos 方块位置
     * @param state 方块状态
     */
    public TileEnderTank(BlockPos pos, BlockState state) {
        super(EnderStorageModContent.ENDER_TANK_TILE.get(), pos, state);
    }

    /**
     * 每游戏刻更新
     * 
     * 执行以下操作：
     * - 更新压力状态（红石信号检测等）
     * - 在压力模式下自动向相邻容器输出液体
     * - 更新液体状态（动画和同步）
     */
    @Override
    public void tick() {
        super.tick();
        assert level != null;
        
        // 更新压力状态（客户端和服务器端）
        pressure_state.update(level.isClientSide);
        
        // 服务器端：在压力模式下执行液体输出
        if (!level.isClientSide && pressure_state.a_pressure) {
            ejectLiquid();
        }

        // 更新液体状态（动画和同步）
        liquid_state.update(level.isClientSide);
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        if (p_155231_ instanceof ServerLevel serverLevel) {
            capCache.setLevelPos(serverLevel, getBlockPos());
        }
    }

    private void ejectLiquid() {
        IFluidHandler source = getStorage();
        for (Direction side : Direction.BY_3D_DATA) {
            IFluidHandler dest = capCache.getCapabilityOr(Capabilities.FluidHandler.BLOCK, side, EmptyFluidHandler.INSTANCE);
            FluidStack drain = source.drain(100, IFluidHandler.FluidAction.SIMULATE);
            if (!drain.isEmpty()) {
                int qty = dest.fill(drain, IFluidHandler.FluidAction.EXECUTE);
                if (qty > 0) {
                    source.drain(qty, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    @Override
    public void onFrequencySet() {
        if (level == null) {
            return;
        }
        if (!level.isClientSide) {
            liquid_state.setFrequency(frequency);
        }
        invalidateCapabilities();
        fluidHandler = null;
    }

    @Override
    public EnderLiquidStorage getStorage() {
        assert level != null;
        return EnderStorageManager.instance(level.isClientSide).getStorage(frequency, EnderLiquidStorage.TYPE);
    }

    @Override
    public void onPlaced(@Nullable LivingEntity entity) {
        assert level != null;
        rotation = entity != null ? (int) Math.floor(entity.getYRot() * 4 / 360 + 2.5D) & 3 : 0;
        pressure_state.b_rotate = pressure_state.a_rotate = pressure_state.approachRotate();
        if (!level.isClientSide) {
            sendUpdatePacket();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("rot", (byte) rotation);
        tag.putBoolean("ir", pressure_state.invert_redstone);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        liquid_state.setFrequency(frequency);
        rotation = tag.getByte("rot") & 3;
        pressure_state.invert_redstone = tag.getBoolean("ir");
    }

    @Override
    public void writeToPacket(MCDataOutput packet) {
        super.writeToPacket(packet);
        packet.writeByte(rotation);
        packet.writeFluidStack(liquid_state.s_liquid);
        packet.writeBoolean(pressure_state.a_pressure);
    }

    @Override
    public void readFromPacket(MCDataInput packet) {
        super.readFromPacket(packet);
        liquid_state.setFrequency(frequency);
        rotation = packet.readUByte() & 3;
        liquid_state.s_liquid = packet.readFluidStack();
        pressure_state.a_pressure = packet.readBoolean();
        if (!described) {
            liquid_state.c_liquid = liquid_state.s_liquid;
            pressure_state.b_rotate = pressure_state.a_rotate = pressure_state.approachRotate();
        }
        described = true;
    }

    @Override
    public boolean activate(Player player, int subHit, InteractionHand hand) {
        if (subHit == 4) {
            pressure_state.invert();
            return true;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, getStorage());
    }

    @Override
    public int getLightValue() {
        if (liquid_state.s_liquid.getAmount() > 0) {
            return FluidUtils.getLuminosity(liquid_state.c_liquid, liquid_state.s_liquid.getAmount() / 16D);
        }

        return 0;
    }

    @Override
    public boolean redstoneInteraction() {
        return true;
    }

    @Override
    public boolean rotate() {
        assert level != null;
        if (!level.isClientSide) {
            rotation = (rotation + 1) % 4;
            sendUpdatePacket();
        }

        return true;
    }

    @Override
    public int comparatorOutput() {
        IFluidTank tank = getStorage();
        FluidStack fluid = tank.getFluid();
        return fluid.getAmount() * 14 / tank.getCapacity() + (fluid.getAmount() > 0 ? 1 : 0);
    }

    public IFluidHandler getFluidHandler() {
        if (fluidHandler == null) {
            fluidHandler = getStorage();
        }
        return fluidHandler;
    }

    public class EnderTankState extends TankSynchroniser.TankState {

        @Override
        public void sendSyncPacket() {
            PacketCustom packet = new PacketCustom(EnderStorageNetwork.NET_CHANNEL, EnderStorageNetwork.C_LIQUID_SYNC, level.registryAccess());
            packet.writePos(getBlockPos());
            packet.writeFluidStack(s_liquid);
            packet.sendToChunk(TileEnderTank.this);
        }

        @Override
        public void onLiquidChanged() {
            assert level != null;
            level.getChunkSource().getLightEngine().checkBlock(worldPosition);
        }
    }

    public class PressureState {

        public boolean invert_redstone;
        public boolean a_pressure;
        public boolean b_pressure;

        public double a_rotate;
        public double b_rotate;

        public void update(boolean client) {
            assert level != null;
            if (client) {
                b_rotate = a_rotate;
                a_rotate = MathHelper.approachExp(a_rotate, approachRotate(), 0.5, 20);
            } else {
                b_pressure = a_pressure;
                a_pressure = level.hasNeighborSignal(getBlockPos()) != invert_redstone;
                if (a_pressure != b_pressure) {
                    sendSyncPacket();
                }
            }
        }

        public double approachRotate() {
            return a_pressure ? -90 : 90;
        }

        private void sendSyncPacket() {
            PacketCustom packet = new PacketCustom(EnderStorageNetwork.NET_CHANNEL, EnderStorageNetwork.C_PRESSURE_SYNC, level.registryAccess());
            packet.writePos(getBlockPos());
            packet.writeBoolean(a_pressure);
            packet.sendToChunk(TileEnderTank.this);
        }

        public void invert() {
            assert level != null;
            invert_redstone = !invert_redstone;
            level.getChunk(worldPosition).setUnsaved(true);
        }
    }
}
