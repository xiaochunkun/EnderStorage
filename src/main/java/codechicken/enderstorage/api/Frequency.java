package codechicken.enderstorage.api;

import codechicken.lib.colour.EnumColour;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.util.Copyable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Created by covers1624 on 4/26/2016.
 */
public final class Frequency implements Copyable<Frequency> {

    private Item left;
    private Item middle;
    private Item right;
    public UUID owner;
    public Component ownerName;

    public Frequency() {
        this(Items.AIR, Items.AIR, Items.AIR);
    }

    public Frequency(Item left, Item middle, Item right) {
        this(left, middle, right, null, null);
    }

    public Frequency(Item left, Item middle, Item right, UUID owner, Component ownerName) {
        this.left = left;
        this.middle = middle;
        this.right = right;
        this.owner = owner;
        this.ownerName = ownerName;
    }

    public Frequency(CompoundTag tagCompound) {
        read_internal(tagCompound);
    }

    public static Frequency fromString(String left, String middle, String right) {
        return fromString(left, middle, right, null, null);
    }

    public static Frequency fromString(String left, String middle, String right, UUID owner, Component ownerName) {
        Item i1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(left));
        Item i2 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(middle));
        Item i3 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(right));
        if (i1 == null || i1 == Items.AIR) {
            throw new RuntimeException(left + " is an invalid item!");
        }
        if (i2 == null || i2 == Items.AIR) {
            throw new RuntimeException(middle + " is an invalid item!");
        }
        if (i3 == null || i3 == Items.AIR) {
            throw new RuntimeException(right + " is an invalid item!");
        }
        return new Frequency(i1, i2, i3, owner, ownerName);
    }

    public Frequency setLeft(Item left) {
        this.left = left == null ? Items.AIR : left;
        return this;
    }

    public Frequency setMiddle(Item middle) {
        this.middle = middle == null ? Items.AIR : middle;
        return this;
    }

    public Frequency setRight(Item right) {
        this.right = right == null ? Items.AIR : right;
        return this;
    }

    public Frequency setOwner(Player player) {
        owner = player.getUUID();
        ownerName = player.getName();
        return this;
    }

    public Frequency clearOwner() {
        owner = null;
        ownerName = null;
        return this;
    }

    public boolean hasOwner() {
        return owner != null && ownerName != null;
    }

    public Frequency set(Item[] items) {
        if (items.length >= 3) {
            setLeft(items[0]);
            setMiddle(items[1]);
            setRight(items[2]);
        }
        return this;
    }

    public Frequency set(Frequency frequency) {
        left = frequency.left;
        middle = frequency.middle;
        right = frequency.right;
        owner = frequency.owner;
        ownerName = frequency.ownerName;
        return this;
    }

    public Item getLeft() {
        return left;
    }

    public Item getMiddle() {
        return middle;
    }

    public Item getRight() {
        return right;
    }

    public ItemStack getLeftStack() {
        return left == null || left == Items.AIR ? ItemStack.EMPTY : new ItemStack(left);
    }

    public ItemStack getMiddleStack() {
        return middle == null || middle == Items.AIR ? ItemStack.EMPTY : new ItemStack(middle);
    }

    public ItemStack getRightStack() {
        return right == null || right == Items.AIR ? ItemStack.EMPTY : new ItemStack(right);
    }

    public UUID getOwner() {
        return owner;
    }

    public Component getOwnerName() {
        return ownerName;
    }

    public Item[] toArray() {
        return new Item[] { left, middle, right };
    }

    private Frequency read_internal(CompoundTag tagCompound) {
        // New format: string IDs under left_item/middle_item/right_item
        if (tagCompound.contains("left_item")) {
            left = readItem(tagCompound.getString("left_item"));
            middle = readItem(tagCompound.getString("middle_item"));
            right = readItem(tagCompound.getString("right_item"));
        } else {
            // Back-compat: old integer wool meta colours -> map to <colour>_dye items
            int l = tagCompound.getInt("left");
            int m = tagCompound.getInt("middle");
            int r = tagCompound.getInt("right");
            left = dyeItemFor(EnumColour.fromWoolMeta(l));
            middle = dyeItemFor(EnumColour.fromWoolMeta(m));
            right = dyeItemFor(EnumColour.fromWoolMeta(r));
        }
        if (tagCompound.hasUUID("owner")) {
            owner = tagCompound.getUUID("owner");
        }
        if (tagCompound.contains("owner_name")) {
            ownerName = Component.Serializer.fromJson(tagCompound.getString("owner_name"));
        }
        return this;
    }

    private static Item readItem(String id) {
        if (id == null || id.isEmpty()) return Items.AIR;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? Items.AIR : item;
    }

    private CompoundTag write_internal(CompoundTag tagCompound) {
        tagCompound.putString("left_item", getId(left));
        tagCompound.putString("middle_item", getId(middle));
        tagCompound.putString("right_item", getId(right));
        if (owner != null) {
            tagCompound.putUUID("owner", owner);
        }
        if (ownerName != null) {
            tagCompound.putString("owner_name", Component.Serializer.toJson(ownerName));
        }
        return tagCompound;
    }

    private static String getId(Item item) {
        if (item == null || item == Items.AIR) return "minecraft:air";
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        return rl == null ? "minecraft:air" : rl.toString();
    }

    public CompoundTag writeToNBT(CompoundTag tagCompound) {
        write_internal(tagCompound);
        return tagCompound;
    }

    public void writeToPacket(MCDataOutput packet) {
        packet.writeCompoundNBT(write_internal(new CompoundTag()));
    }

    public static Frequency readFromPacket(MCDataInput packet) {
        return new Frequency(packet.readCompoundNBT());
    }

    public static Frequency readFromStack(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag stackTag = stack.getTag();
            if (stackTag.contains("Frequency")) {
                return new Frequency(stackTag.getCompound("Frequency"));
            }
        }
        return new Frequency();
    }

    public ItemStack writeToStack(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        tagCompound.put("Frequency", write_internal(new CompoundTag()));
        return stack;
    }

    public String toModelLoc() {
        return "left=" + getId(getLeft()) + ",middle=" + getId(getMiddle()) + ",right=" + getId(getRight()) + ",owned=" + hasOwner();
    }

    @Override
    public String toString() {
        String owner = "";
        if (hasOwner()) {
            owner = ",owner=" + this.owner;
        }
        return "left=" + getId(getLeft()) + ",middle=" + getId(getMiddle()) + ",right=" + getId(getRight()) + owner;
    }

    public Component getTooltip() {
        Component l = getLeftStack().isEmpty() ? Component.literal("empty") : getLeftStack().getHoverName();
        Component m = getMiddleStack().isEmpty() ? Component.literal("empty") : getMiddleStack().getHoverName();
        Component r = getRightStack().isEmpty() ? Component.literal("empty") : getRightStack().getHoverName();
        return l.copy().append("/").append(m).append("/").append(r);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public Frequency copy() {
        return new Frequency(this.left, this.middle, this.right, this.owner, this.ownerName);
    }

    // Utility: map EnumColour (legacy) -> dye item type.
    public static Item dyeItemFor(EnumColour colour) {
        if (colour == null) return Items.AIR;
        String name = colour.getSerializedName();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", name + "_dye"));
        return item == null ? Items.AIR : item;
    }
}
