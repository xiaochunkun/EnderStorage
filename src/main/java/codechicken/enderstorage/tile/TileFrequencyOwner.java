package codechicken.enderstorage.tile;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.network.EnderStorageNetwork;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileFrequencyOwner extends BlockEntity {

    // Chest 尺寸（较大）
    public static final double CHEST_SLOT_HALF = 3D / 16D; // 半径（宽 6/16）
    public static final double CHEST_SLOT_HEIGHT = 1D / 16D;
    public static final float CHEST_ITEM_SCALE = 0.18F;
    // Tank 尺寸（较小）
    public static final double TANK_SLOT_HALF = 2.5D / 16D; // 半径（宽 5/16）
    public static final double TANK_SLOT_HEIGHT = 1D / 16D;
    public static final float TANK_ITEM_SCALE = 0.16F;
    // 槽位选取盒：正方形（Chest / Tank 区分）
    public static final Cuboid6 CHEST_SELECTION_BUTTON = new Cuboid6(-CHEST_SLOT_HALF, 0, -CHEST_SLOT_HALF, CHEST_SLOT_HALF, CHEST_SLOT_HEIGHT, CHEST_SLOT_HALF);
    public static final Cuboid6 TANK_SELECTION_BUTTON = new Cuboid6(-TANK_SLOT_HALF, 0, -TANK_SLOT_HALF, TANK_SLOT_HALF, TANK_SLOT_HEIGHT, TANK_SLOT_HALF);

    protected Frequency frequency = new Frequency();
    private int changeCount;

    public TileFrequencyOwner(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFreq(Frequency frequency) {
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
        if (getStorage().getChangeCount() > changeCount) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            changeCount = getStorage().getChangeCount();
        }
    }

    public abstract AbstractEnderStorage getStorage();

    public void onFrequencySet() {
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        frequency.set(new Frequency(tag.getCompound("Frequency")));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("Frequency", frequency.writeToNBT(new CompoundTag()));
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        onFrequencySet();
    }

    public boolean activate(Player player, int subHit, InteractionHand hand) {
        return false;
    }

    public void onNeighborChange(BlockPos from) {
    }

    public void onPlaced(LivingEntity entity) {
    }

    protected void sendUpdatePacket() {
        createPacket().sendToChunk(level, getBlockPos().getX() >> 4, getBlockPos().getZ() >> 4);
    }

    public PacketCustom createPacket() {
        PacketCustom packet = new PacketCustom(EnderStorageNetwork.NET_CHANNEL, EnderStorageNetwork.C_TILE_UPDATE);
        packet.writePos(getBlockPos());
        writeToPacket(packet);
        return packet;
    }

    public void writeToPacket(MCDataOutput packet) {
        frequency.writeToPacket(packet);
    }

    public void readFromPacket(MCDataInput packet) {
        frequency.set(Frequency.readFromPacket(packet));
        onFrequencySet();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    public int getLightValue() {
        return 0;
    }

    public boolean redstoneInteraction() {
        return false;
    }

    public int comparatorInput() {
        return 0;
    }

    public boolean rotate() {
        return false;
    }
}
