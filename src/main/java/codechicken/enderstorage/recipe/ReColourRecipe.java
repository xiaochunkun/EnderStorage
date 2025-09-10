package codechicken.enderstorage.recipe;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.lib.colour.EnumColour;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.covers1624.quack.collection.ColUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 末影存储重新着色配方类
 * 
 * 这是一个特殊的合成配方，允许玩家使用染料来重新设置
 * 已有末影存储设备的频率颜色。这个配方不是标准的有形配方，
 * 而是实现了CraftingRecipe接口的自定义配方。
 * 
 * 配方工作原理：
 * - 将末影存储设备放在合成网格中
 * - 在设备上方放置最多3个染料来设置新的频率
 * - 染料的位置决定频率的左、中、右颜色
 * - 可以使用颜色混合来创建新的颜色组合
 * 
 * 位置映射：
 * - 末影设备左边的染料 -> 频率左颜色
 * - 末影设备正上方的染料 -> 频率中颜色  
 * - 末影设备右边的染料 -> 频率右颜色
 * 
 * @author covers1624
 * @since 8/07/2017
 */
public class ReColourRecipe implements CraftingRecipe {

    /** 配方组名称 */
    protected final String group;
    /** 配方结果物品（末影存储设备） */
    protected final ItemStack result;
    /** 配方材料限制（用于匹配可被重新着色的物品） */
    protected final Ingredient ingredient;

    /**
     * 构造函数（简化版）
     * 
     * 使用默认空组名创建重新着色配方。
     * 
     * @param result 配方结果物品
     */
    public ReColourRecipe(ItemStack result) {
        this("", result);
    }

    /**
     * 构造函数（完整版）
     * 
     * 创建一个新的重新着色配方。
     * 
     * @param group 配方组名称
     * @param result 配方结果物品
     */
    public ReColourRecipe(String group, ItemStack result) {
        this.group = group;
        this.result = result;
        // 创建材料限制，只允许相同类型的末影存储设备
        ingredient = Ingredient.of(result);
    }

    /**
     * 检查配方是否匹配
     * 
     * 验证合成网格中的物品是否符合重新着色配方的要求：
     * 1. 网格不能为空
     * 2. 必须有一个末影存储设备
     * 3. 设备不能在第一行（上方需要放置染料）
     * 4. 染料必须放在设备上方的正确位置
     * 5. 没有其他无关物品
     * 
     * @param inv 合成输入网格
     * @param worldIn 世界实例
     * @return true 如果配方匹配
     */
    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        // 空网格不匹配
        if (inv.isEmpty()) {
            return false;
        }
        // 查找末影存储设备
        ItemWithPos chest = findChest(inv);
        // 设备不存在或在第一行（y=0）则不匹配
        if (chest == null || chest.y == 0) {
            return false;
        }
        // 记录有效位置（包含设备和染料）
        List<ItemWithPos> validPositions = new ArrayList<>();
        validPositions.add(chest);

        // 查找并验证染料位置
        EnumColour[] colours = findDyes(inv, chest, validPositions);
        if (colours == null) return false;

        // 检查是否有无关物品
        for (int x = 0; x < inv.width(); x++) {
            for (int y = 0; y < inv.height(); y++) {
                ItemStack stack = inv.getItem(x + y * inv.width());
                // 如果有物品但不在有效位置列表中，则不匹配
                if (!stack.isEmpty() && !validPositions.contains(new ItemWithPos(x, y, stack))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 组装配方结果
     * 
     * 根据合成网格中的设备和染料创建新的带有更新频率的设备。
     * 
     * @param inv 合成输入网格
     * @param registries 注册表提供者
     * @return 带有新频率的末影存储设备
     */
    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        // 查找末影存储设备
        ItemWithPos chestPos = findChest(inv);
        if (chestPos == null) return result.copy(); // 这不应该发生...

        // 查找染料颜色
        EnumColour[] colours = findDyes(inv, chestPos, null);
        if (colours == null) return result.copy(); // 这也不应该发生...

        // 从原设备中读取频率，更新颜色，然后写入新的结果物品
        return Frequency.readFromStack(chestPos.stack)
                .withColours(colours)
                .writeToStack(result.copy());
    }

    /**
     * 是否为特殊配方
     * 
     * 返回true表示这是一个特殊的自定义配方，
     * 不遵循标准的有形或无形配方规则。
     * 
     * @return true
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    /**
     * 获取配方序列化器
     * 
     * @return 重新着色配方序列化器
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return EnderStorageModContent.RECOLOUR_RECIPE_SERIALIZER.get();
    }

    /**
     * 检查是否可以在指定尺寸的网格中制作
     * 
     * 重新着色配方需要至少3x3的网格来放置设备和染料。
     * 
     * @param width 网格宽度
     * @param height 网格高度
     * @return true 如果可以在该尺寸中制作
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    /**
     * 获取配方组名称
     * 
     * @return 配方组名称
     */
    @Override
    public String getGroup() {
        return group;
    }

    /**
     * 获取配方结果物品
     * 
     * @param registries 注册表提供者
     * @return 配方结果物品
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    /**
     * 获取合成书类别
     * 
     * @return 杂项类别
     */
    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    /**
     * 在合成网格中查找末影存储设备
     * 
     * 扫描整个合成网格，查找第一个匹配配方材料限制的物品。
     * 
     * @param inv 合成输入网格
     * @return 末影存储设备的位置和物品，没有找到则返回null
     */
    private @Nullable ItemWithPos findChest(CraftingInput inv) {
        ItemWithPos chest = null;
        // 遍历合成网格查找匹配的设备
        for (int x = 0; x < inv.width(); x++) {
            for (int y = 0; y < inv.height(); y++) {
                ItemStack stack = inv.getItem(x, y);
                if (stack.isEmpty()) continue;
                // 检查是否匹配配方材料限制
                if (!ingredient.test(stack)) continue;

                chest = new ItemWithPos(x, y, stack);
                break; // 找到第一个匹配的就退出
            }
        }
        return chest;
    }

    /**
     * 在合成网格中查找染料并确定颜色
     * 
     * 根据染料相对于末影存储设备的位置来确定频率的三个颜色：
     * - 设备左侧的染料 -> 左颜色 (index 0)
     * - 设备正上方的染料 -> 中颜色 (index 1)
     * - 设备右侧的染料 -> 右颜色 (index 2)
     * 
     * 同一位置的多个染料会进行颜色混合。
     * 
     * @param inv 合成输入网格
     * @param chest 末影存储设备的位置和物品
     * @param validPositions 有效位置列表（可为null），用于记录找到的染料位置
     * @return 三个颜色的数组，没有找到有效染料则返回null
     */
    private EnumColour @Nullable [] findDyes(CraftingInput inv, ItemWithPos chest, @Nullable List<ItemWithPos> validPositions) {
        // 初始化三个颜色位置，都为null
        EnumColour[] colours = new EnumColour[] { null, null, null };
        // 扫描设备上方的所有位置查找染料
        for (int x = 0; x < inv.width(); x++) {
            for (int y = 0; y < chest.y; y++) { // 只检查设备上方的行
                ItemStack stack = inv.getItem(x, y);
                if (stack.isEmpty()) continue;

                // 尝试从物品中获取染料颜色
                EnumColour colour = EnumColour.fromDyeStack(stack);
                if (colour == null) continue; // 不是染料

                // 根据相对位置确定颜色索引
                // 设备正上方=1，右上=2，左上=0
                int effectiveColour = chest.x == x ? 1 : chest.x < x ? 2 : 0;

                // 处理颜色混合或设置新颜色
                if (colours[effectiveColour] != null) {
                    // 已经有颜色，尝试混合
                    EnumColour merge = EnumColour.mix(colours[effectiveColour], colour);
                    if (merge == null || merge == colour) return null; // 混合失败

                    colours[effectiveColour] = merge;
                } else {
                    // 设置新颜色
                    colours[effectiveColour] = colour;
                }
                // 如果提供了有效位置列表，则记录这个染料位置
                if (validPositions != null) {
                    validPositions.add(new ItemWithPos(x, y, stack));
                }
            }
        }
        // 返回颜色数组，如果所有颜色都是null则返回null
        return !ColUtils.allMatch(colours, Objects::isNull) ? colours : null;
    }

    /**
     * 物品位置记录
     * 
     * 用于记录合成网格中物品的位置和内容。
     * 
     * @param x 横坐标
     * @param y 纵坐标
     * @param stack 物品堆叠
     */
    private record ItemWithPos(int x, int y, ItemStack stack) { }

    /**
     * 重新着色配方序列化器
     * 
     * 负责将末影存储重新着色配方在JSON文件和网络数据之间进行
     * 序列化和反序列化。由于这是一个特殊配方，只需要存储
     * 配方组和结果物品信息。
     */
    public static class Serializer implements RecipeSerializer<ReColourRecipe> {

        /** JSON编解码器 - 用于从配方JSON文件中读取配方数据 */
        private static final MapCodec<ReColourRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(e -> e.group),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(e -> e.result)
                ).apply(builder, ReColourRecipe::new)
        );

        /** 流编解码器 - 用于在网络中传输配方数据 */
        private static final StreamCodec<RegistryFriendlyByteBuf, ReColourRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, e -> e.group,
                ItemStack.STREAM_CODEC, e -> e.result,
                ReColourRecipe::new
        );

        /**
         * 获取JSON编解码器
         * 
         * @return JSON编解码器
         */
        @Override
        public MapCodec<ReColourRecipe> codec() {
            return CODEC;
        }

        /**
         * 获取流编解码器
         * 
         * @return 流编解码器
         */
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ReColourRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
