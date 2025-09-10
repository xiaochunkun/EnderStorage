package codechicken.enderstorage.recipe;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.lib.colour.EnumColour;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

/**
 * 末影存储创建配方类
 * 
 * 继承自ShapedRecipe，用于处理末影存储设备的创建配方。
 * 这个配方类的特殊之处在于它会根据配方中使用的羊毛颜色
 * 自动设置创建出的末影存储设备的频率。
 * 
 * 配方逻辑：
 * - 扩展标准的有形配方系统
 * - 在配方执行时扫描合成网格中的羊毛
 * - 将找到的羊毛颜色作为频率的三个颜色值
 * - 输出带有正确频率的末影存储设备
 * 
 * 注意：创建配方只使用单一颜色的羊毛，会将该颜色设置为
 * 频率的左、中、右三个位置，形成单色频率。
 * 
 * @author covers1624
 * @since 1/11/19
 */
public class CreateRecipe extends ShapedRecipe {

    /**
     * 构造函数
     * 
     * 创建一个新的末影存储创建配方。
     * 
     * @param group 配方组名称
     * @param pattern 有形配方模式
     * @param result 配方结果物品
     */
    public CreateRecipe(String group, ShapedRecipePattern pattern, ItemStack result) {
        super(group, CraftingBookCategory.MISC, pattern, result.copy(), true);
    }

    /**
     * 组装配方结果
     * 
     * 根据合成网格中的物品组装最终的配方结果。
     * 这个方法会扫描合成网格中的羊毛，并将羊毛的颜色
     * 设置为末影存储设备的频率。
     * 
     * @param inv 合成输入网格
     * @param registries 注册表提供者
     * @return 带有频率的末影存储设备
     */
    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        // 默认颜色为白色
        EnumColour colour = EnumColour.WHITE;
        // 创建配方只使用一个羊毛
        // 我们找到它并将颜色设置为该颜色
        finish:
        // 遍历合成网格查找羊毛
        for (int x = 0; x < inv.width(); x++) {
            for (int y = 0; y < inv.height(); y++) {
                ItemStack stack = inv.getItem(x, y);
                if (!stack.isEmpty()) {
                    // 尝试从物品堆叠中获取颜色
                    EnumColour c = EnumColour.fromWoolStack(stack);
                    if (c != null) {
                        colour = c;
                        break finish; // 找到第一个羊毛后退出
                    }
                }
            }
        }
        // 创建频率（使用相同颜色作为左、中、右三个位置）
        Frequency frequency = new Frequency(colour, colour, colour);
        // 将频率写入并返回结果物品
        return frequency.writeToStack(super.assemble(inv, registries));
    }

    /**
     * 获取配方序列化器
     * 
     * @return 创建配方序列化器
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return EnderStorageModContent.CREATE_RECIPE_SERIALIZER.get();
    }

    /**
     * 创建配方序列化器
     * 
     * 负责将末影存储创建配方在JSON文件和网络数据之间进行序列化和反序列化。
     */
    public static class Serializer implements RecipeSerializer<CreateRecipe> {

        /** JSON编解码器 - 用于从配方JSON文件中读取配方数据 */
        private static final MapCodec<CreateRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(e -> e.group),
                        ShapedRecipePattern.MAP_CODEC.forGetter(e -> e.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(e -> e.result)
                ).apply(builder, CreateRecipe::new)
        );

        /** 流编解码器 - 用于在网络中传输配方数据 */
        private static final StreamCodec<RegistryFriendlyByteBuf, CreateRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, e -> e.group,
                ShapedRecipePattern.STREAM_CODEC, e -> e.pattern,
                ItemStack.STREAM_CODEC, e -> e.result,
                CreateRecipe::new
        );

        /**
         * 获取JSON编解码器
         * 
         * @return JSON编解码器
         */
        @Override
        public MapCodec<CreateRecipe> codec() {
            return CODEC;
        }

        /**
         * 获取流编解码器
         * 
         * @return 流编解码器
         */
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CreateRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
