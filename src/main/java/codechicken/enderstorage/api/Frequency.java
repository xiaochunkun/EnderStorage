package codechicken.enderstorage.api;

import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * 末影存储频率类
 * 
 * 这是末影存储系统的核心类，定义了存储访问的频率标识。每个频率由三个颜色组成，
 * 形成一个独特的标识符，用于区分不同的存储空间。相同频率的末影存储设备
 * （末影箱子、末影储罐等）将共享同一个存储空间。
 * 
 * 频率组成：
 * - left: 左侧颜色
 * - middle: 中间颜色  
 * - right: 右侧颜色
 * - owner: 可选的拥有者UUID，用于私人频率
 * - ownerName: 可选的拥有者显示名称
 * 
 * 频率系统特点：
 * - 公共频率：任何玩家都可以访问的频率（无拥有者）
 * - 私人频率：只有特定玩家可以访问的频率（有拥有者）
 * - 跨维度共享：相同频率在所有维度中共享存储内容
 * - 持久化：频率及其对应的存储数据会被保存到磁盘
 * 
 * @author covers1624
 * @since 4/26/2016
 */
public record Frequency(
        EnumColour left,    // 左侧颜色
        EnumColour middle,  // 中间颜色
        EnumColour right,   // 右侧颜色
        Optional<UUID> owner,      // 拥有者UUID（私人频率）
        Optional<Component> ownerName  // 拥有者显示名称
) {

    /** 用于数据序列化的Codec，支持从JSON/NBT等格式读写频率数据 */
    public static final Codec<Frequency> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    EnumColour.CODEC.fieldOf("left").forGetter(Frequency::left),
                    EnumColour.CODEC.fieldOf("middle").forGetter(Frequency::middle),
                    EnumColour.CODEC.fieldOf("right").forGetter(Frequency::right),
                    UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(Frequency::owner),
                    ComponentSerialization.CODEC.optionalFieldOf("ownerName").forGetter(Frequency::ownerName)
            ).apply(builder, Frequency::new)
    );
    
    /** 用于网络传输的StreamCodec，支持在客户端和服务端之间传输频率数据 */
    public static final StreamCodec<RegistryFriendlyByteBuf, Frequency> STREAM_CODEC = StreamCodec.composite(
            EnumColour.STREAM_CODEC, Frequency::left,
            EnumColour.STREAM_CODEC, Frequency::middle,
            EnumColour.STREAM_CODEC, Frequency::right,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), Frequency::owner,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), Frequency::ownerName,
            Frequency::new
    );

    /**
     * 默认构造函数
     * 创建一个白色频率（白-白-白），无拥有者的公共频率
     */
    public Frequency() {
        this(EnumColour.WHITE, EnumColour.WHITE, EnumColour.WHITE);
    }

    /**
     * 颜色构造函数
     * 创建指定颜色组合的公共频率（无拥有者）
     * 
     * @param left 左侧颜色
     * @param middle 中间颜色
     * @param right 右侧颜色
     */
    public Frequency(EnumColour left, EnumColour middle, EnumColour right) {
        this(left, middle, right, Optional.empty(), Optional.empty());
    }

    /**
     * 从NBT标签构造频率（已弃用）
     * 
     * 从旧版本的NBT数据格式中读取频率信息。
     * 此方法主要用于向后兼容性，不建议在新代码中使用。
     * 
     * @param tagCompound 包含频率数据的NBT标签
     * @deprecated 请使用CODEC进行序列化和反序列化
     */
    @Deprecated
    public Frequency(CompoundTag tagCompound) {
        this(
                EnumColour.fromWoolMeta(tagCompound.getInt("left")),
                EnumColour.fromWoolMeta(tagCompound.getInt("middle")),
                EnumColour.fromWoolMeta(tagCompound.getInt("right")),
                tagCompound.hasUUID("owner") ? Optional.of(tagCompound.getUUID("owner")) : Optional.empty(),
                tagCompound.contains("owner_name") ? Optional.of(Component.Serializer.fromJson(tagCompound.getString("owner_name"), RegistryAccess.EMPTY)) : Optional.empty()
        );
    }

    /**
     * 创建具有新左侧颜色的频率副本
     * 
     * @param left 新的左侧颜色，如果为null则返回原频率
     * @return 具有新左侧颜色的频率副本，或原频率（如果left为null）
     */
    public Frequency withLeft(@Nullable EnumColour left) {
        if (left != null) {
            return new Frequency(left, middle, right, owner, ownerName);
        }
        return this;
    }

    /**
     * 创建具有新中间颜色的频率副本
     * 
     * @param middle 新的中间颜色，如果为null则返回原频率
     * @return 具有新中间颜色的频率副本，或原频率（如果middle为null）
     */
    public Frequency withMiddle(@Nullable EnumColour middle) {
        if (middle != null) {
            return new Frequency(left, middle, right, owner, ownerName);
        }
        return this;
    }

    /**
     * 创建具有新右侧颜色的频率副本
     * 
     * @param right 新的右侧颜色，如果为null则返回原频率
     * @return 具有新右侧颜色的频率副本，或原频率（如果right为null）
     */
    public Frequency withRight(@Nullable EnumColour right) {
        if (right != null) {
            return new Frequency(left, middle, right, owner, ownerName);
        }
        return this;
    }

    /**
     * 创建具有指定拥有者的私人频率副本
     * 
     * 将当前频率转换为私人频率，只有指定的玩家才能访问。
     * 私人频率通过玩家的UUID和显示名称进行标识。
     * 
     * @param player 拥有该频率的玩家
     * @return 具有指定拥有者的私人频率副本
     */
    public Frequency withOwner(Player player) {
        return new Frequency(left, middle, right, Optional.of(player.getUUID()), Optional.of(player.getName()));
    }

    /**
     * 创建移除拥有者的公共频率副本
     * 
     * 将私人频率转换为公共频率，任何玩家都可以访问。
     * 
     * @return 无拥有者的公共频率副本
     */
    public Frequency withoutOwner() {
        return new Frequency(left, middle, right, Optional.empty(), Optional.empty());
    }

    /**
     * 检查频率是否有拥有者
     * 
     * @return 如果频率有拥有者（私人频率）则返回true，否则返回false（公共频率）
     */
    public boolean hasOwner() {
        return owner.isPresent() && ownerName.isPresent();
    }

    /**
     * 创建具有指定颜色组合的频率副本
     * 
     * 传入颜色数组，更新频率的三个颜色值。数组必须包含三个元素，
     * 分别对应左、中、右三个位置的颜色。
     * 
     * @param colours 长度为3的颜色数组 [left, middle, right]
     * @return 具有新颜色组合的频率副本
     */
    public Frequency withColours(@Nullable EnumColour[] colours) {
        return withLeft(colours[0])
                .withMiddle(colours[1])
                .withRight(colours[2]);
    }

    /**
     * 将频率的颜色转换为数组格式
     * 
     * @return 包含三个颜色的数组 [left, middle, right]
     */
    public EnumColour[] toArray() {
        return new EnumColour[] { left, middle, right };
    }

    /**
     * 内部NBT写入方法
     * 
     * 将频率的所有数据序列化到指定的NBT标签中。
     * 包括三个颜色的羊毛元数据值以及可选的拥有者信息。
     * 
     * @param tagCompound 要写入数据的NBT标签
     * @return 包含频率数据的NBT标签
     */
    private CompoundTag write_internal(CompoundTag tagCompound) {
        // 将颜色转换为羊毛元数据值存储
        tagCompound.putInt("left", left.getWoolMeta());
        tagCompound.putInt("middle", middle.getWoolMeta());
        tagCompound.putInt("right", right.getWoolMeta());
        
        // 如果有拥有者信息，则一并存储
        owner.ifPresent(uuid -> tagCompound.putUUID("owner", uuid));
        ownerName.ifPresent(component -> tagCompound.putString("owner_name", Component.Serializer.toJson(component, RegistryAccess.EMPTY)));
        
        return tagCompound;
    }

    /**
     * 将频率数据写入网络数据包（已弃用）
     * 
     * @param packet 网络数据输出流
     * @deprecated 请使用STREAM_CODEC进行网络传输
     */
    @Deprecated
    public void writeToPacket(MCDataOutput packet) {
        packet.writeCompoundNBT(write_internal(new CompoundTag()));
    }

    /**
     * 从网络数据包读取频率数据（已弃用）
     * 
     * @param packet 网络数据输入流
     * @return 从数据包中解析的频率实例
     * @deprecated 请使用STREAM_CODEC进行网络传输
     */
    @Deprecated
    public static Frequency readFromPacket(MCDataInput packet) {
        return new Frequency(packet.readCompoundNBT());
    }

    /**
     * 从物品堆叠中读取频率数据（可能被弃用）
     * 
     * 从末影存储物品的数据组件中读取频率信息。
     * 如果物品没有频率数据，则返回默认的白色频率。
     * 
     * @param stack 包含频率数据的物品堆叠
     * @return 从物品中读取的频率，或默认频率
     * @deprecated 可能被弃用
     */
    @Deprecated // Maybe?
    public static Frequency readFromStack(ItemStack stack) {
        return stack.getOrDefault(EnderStorageModContent.FREQUENCY_DATA_COMPONENT, new Frequency());
    }

    /**
     * 将频率数据写入物品堆叠（可能被弃用）
     * 
     * 将当前频率作为数据组件存储在末影存储物品中。
     * 这样物品就会记住其频率设置。
     * 
     * @param stack 要写入频率数据的物品堆叠
     * @return 包含频率数据的物品堆叠
     * @deprecated 可能被弃用
     */
    @Deprecated // Maybe?
    public ItemStack writeToStack(ItemStack stack) {
        stack.set(EnderStorageModContent.FREQUENCY_DATA_COMPONENT, this);
        return stack;
    }

    /**
     * 转换为字符串表示
     * 
     * 生成人类可读的频率字符串，包含三个颜色的名称和可选的拥有者信息。
     * 格式："left=颜色,middle=颜色,right=颜色[,owner=UUID]"
     * 
     * @return 频率的字符串表示
     */
    @Override
    public String toString() {
        String owner = "";
        if (hasOwner()) {
            owner = ",owner=" + this.owner;
        }
        return "left=" + left().getSerializedName() + ",middle=" + middle().getSerializedName() + ",right=" + right().getSerializedName() + owner;
    }

    /**
     * 生成用于显示的工具提示组件
     * 
     * 创建一个用于在游戏中显示的文本组件，展示频率的三个颜色。
     * 格式："颜色名/颜色名/颜色名"，使用本地化的颜色名称。
     * 
     * @return 用于显示的文本组件
     */
    public Component getTooltip() {
        return Component.translatable(left().getUnlocalizedName())
                .append("/")
                .append(Component.translatable(middle().getUnlocalizedName()))
                .append("/")
                .append(Component.translatable(right().getUnlocalizedName()));
    }
}
