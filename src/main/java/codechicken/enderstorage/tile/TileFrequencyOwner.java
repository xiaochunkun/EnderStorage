package codechicken.enderstorage.tile;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.network.EnderStorageNetwork;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 频率拥有者方块实体抽象基类
 * 
 * 为所有支持频率系统的EnderStorage方块实体提供基础功能，包括：
 * - 频率管理：存储、设置和同步频率信息
 * - 网络通信：处理频率相关的网络数据包
 * - 用户交互：支持频率选择按钮的点击检测
 * - 状态同步：监控存储变化并更新红石输出
 * 
 * 核心设计：
 * - 抽象基类模式：为不同类型的存储方块提供统一的频率管理
 * - 事件驱动：响应频率变化、用户交互等事件
 * - 网络优化：只在必要时发送更新数据包
 * - 红石兼容：支持比较器输出和红石信号
 * 
 * 继承层次：
 * - TileEnderChest: 物品存储方块实体
 * - TileEnderTank: 液体存储方块实体
 * 
 * @author EnderStorage Team
 * @since 1.0.0
 */
public abstract class TileFrequencyOwner extends BlockEntity {

    public static final Cuboid6 SELECTION_BUTTON = new Cuboid6(-1 / 16D, 0, -2 / 16D, 1 / 16D, 1 / 16D, 2 / 16D);

    protected Frequency frequency = new Frequency();
    private int changeCount;

    public TileFrequencyOwner(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFreq(Frequency frequency) {
        assert level != null;
        this.frequency = frequency;
        onFrequencySet();
        setChanged();
        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
        if (!level.isClientSide) {
            sendUpdatePacket();
        }
    }

    public void tick() {
        assert level != null;
        if (getStorage().getChangeCount() > changeCount) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            changeCount = getStorage().getChangeCount();
        }
    }

    public abstract AbstractEnderStorage getStorage();

    public void onFrequencySet() {
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        frequency = Frequency.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("Frequency"))
                .getOrThrow();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("Frequency", Frequency.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), frequency)
                .getOrThrow()
        );
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        onFrequencySet();
    }

    public boolean activate(Player player, int subHit, InteractionHand hand) {
        return false;
    }

    public void onPlaced(@Nullable LivingEntity entity) {
    }

    protected void sendUpdatePacket() {
        assert level != null;
        createPacket().sendToChunk(this);
    }

    public PacketCustom createPacket() {
        PacketCustom packet = new PacketCustom(EnderStorageNetwork.NET_CHANNEL, EnderStorageNetwork.C_TILE_UPDATE, level.registryAccess());
        packet.writePos(getBlockPos());
        writeToPacket(packet);
        return packet;
    }

    public void writeToPacket(MCDataOutput packet) {
        packet.writeWithRegistryCodec(Frequency.STREAM_CODEC, frequency);
    }

    public void readFromPacket(MCDataInput packet) {
        frequency = packet.readWithRegistryCodec(Frequency.STREAM_CODEC);
        onFrequencySet();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadWithComponents(tag, registries);
    }

    public int getLightValue() {
        return 0;
    }

    public boolean redstoneInteraction() {
        return false;
    }

    public int comparatorOutput() {
        return 0;
    }

    public boolean rotate() {
        return false;
    }
}
