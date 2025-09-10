package codechicken.enderstorage.init;

import codechicken.enderstorage.client.render.item.EnderChestItemRender;
import codechicken.enderstorage.client.render.item.EnderTankItemRender;
import codechicken.enderstorage.recipe.CreateRecipe;
import codechicken.enderstorage.recipe.ReColourRecipe;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.datagen.ItemModelProvider;
import codechicken.lib.datagen.recipe.RecipeProvider;
import codechicken.lib.util.CCLTags;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static codechicken.enderstorage.EnderStorage.MOD_ID;
import static codechicken.enderstorage.init.EnderStorageModContent.*;

/**
 * 数据生成器管理类
 * <p>
 * 负责管理和注册所有的游戏数据生成器，包括：
 * - 物品模型生成器：生成物品的JSON模型文件
 * - 方块状态生成器：生成方块状态和模型文件
 * - 方块标签生成器：生成方块的数据标签
 * - 配方生成器：生成合成配方JSON文件
 * <p>
 * 数据生成器在开发时运行，用于自动生成游戏所需的各种资源文件，
 * 避免手动创建大量的JSON文件，提高开发效率并减少错误。
 * 
 * @author covers1624 创建于 2020年4月25日
 */
public class DataGenerators {

    /** 防止重复初始化的崩溃锁 */
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    /**
     * 数据生成器初始化方法
     * <p>
     * 注册数据收集事件监听器，当数据生成事件触发时，
     * 会自动注册所有必要的数据生成器。
     * 
     * @param modBus 模组事件总线
     */
    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(DataGenerators::gatherDataGenerators);
    }

    /**
     * 数据生成器收集事件处理方法
     * <p>
     * 当数据生成事件触发时，注册所有需要的数据生成器：
     * - 方块状态生成器（客户端侧）
     * - 物品模型生成器（客户端侧）
     * - 方块标签生成器（服务端侧）
     * - 配方生成器（服务端侧）
     * 
     * @param event 数据收集事件
     */
    private static void gatherDataGenerators(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        gen.addProvider(event.includeClient(), new BlockStates(output, files));
        gen.addProvider(event.includeClient(), new ItemModels(output, files));
        gen.addProvider(event.includeServer(), new BlockTagGen(output, lookupProvider, files));
        gen.addProvider(event.includeServer(), new Recipes(lookupProvider, output));
    }

    /**
     * 物品模型生成器
     * <p>
     * 生成所有末影存储物品的模型文件，包括：
     * - 末影箱和末影储罐的物品模型（使用自定义渲染器）
     * - 末影背包的复合模型（包含多个状态和颜色变体）
     * <p>
     * 末影背包使用复合模型系统，根据不同的属性谓词显示不同的纹理：
     * - 基础袋子模型（打开/关闭状态，有无所有者）
     * - 三个颜色按钮模型（左、中、右，每个有16种颜色）
     */
    private static class ItemModels extends ItemModelProvider {

        /**
         * 构造物品模型生成器
         * 
         * @param output 输出包装器
         * @param existingFileHelper 现有文件助手
         */
        public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, MOD_ID, existingFileHelper);
        }

        /**
         * 注册所有物品模型
         * <p>
         * 为末影箱和末影储罐注册自定义渲染器类，
         * 为末影背包生成复杂的复合模型结构。
         */
        @Override
        protected void registerModels() {
            // 注册末影箱和末影储罐的自定义渲染器
            clazz(ENDER_CHEST_ITEM, EnderChestItemRender.class);
            clazz(ENDER_TANK_ITEM, EnderTankItemRender.class);

            // 创建末影背包的复合模型
            CompositeLoaderBuilder bag = generated(ENDER_POUCH)
                    .noTexture()
                    .customLoader(CompositeLoaderBuilder::ccl)
                    // 创建基础袋子模型（根据打开状态和所有者状态显示不同纹理）
                    .nested("bag", e -> {
                        e.parent(GENERATED).noTexture();
                        // 关闭状态，无所有者
                        e.override(o -> {
                            o.predicate(modLoc("open"), 0);
                            o.predicate(modLoc("owned"), 0);
                            o.model("ender_pouch_closed", m -> m.parent(GENERATED).texture("layer0", modLoc("item/pouch/closed")));
                        });
                        // 打开状态，无所有者
                        e.override(o -> {
                            o.predicate(modLoc("open"), 1);
                            o.predicate(modLoc("owned"), 0);
                            o.model("ender_pouch_open", m -> m.parent(GENERATED).texture("layer0", modLoc("item/pouch/open")));
                        });
                        // 关闭状态，有所有者
                        e.override(o -> {
                            o.predicate(modLoc("open"), 0);
                            o.predicate(modLoc("owned"), 1);
                            o.model("ender_pouch_owned_closed", m -> m.parent(GENERATED).texture("layer0", modLoc("item/pouch/owned_closed")));
                        });
                        // 打开状态，有所有者
                        e.override(o -> {
                            o.predicate(modLoc("open"), 1);
                            o.predicate(modLoc("owned"), 1);
                            o.model("ender_pouch_owned_open", m -> m.parent(GENERATED).texture("layer0", modLoc("item/pouch/owned_open")));
                        });
                    });
            // 为每个颜色按钮位置（左、中、右）生成模型
            for (String side : new String[] { "left", "middle", "right" }) {
                bag.nested(side, e -> {
                    e.parent(GENERATED).noTexture();
                    // 为每种颜色生成对应的按钮模型
                    for (EnumColour colour : EnumColour.values()) {
                        String col = colour.getSerializedName();
                        e.override(o -> {
                            o.predicate(modLoc(side), colour.ordinal());
                            o.model("ender_pouch_button_" + side + "_" + col, m -> {
                                m.parent(GENERATED).texture(modLoc("item/pouch/buttons/" + side + "/" + col));
                            });
                        });
                    }
                });
            }
        }

        @Override
        public String getName() {
            return "EnderStorage Item models";
        }
    }

    /**
     * 方块状态生成器
     * <p>
     * 生成方块的状态文件和模型文件。
     * 由于末影箱和末影储罐使用自定义渲染器，
     * 这里只生成简单的占位模型。
     */
    private static class BlockStates extends BlockStateProvider {

        /**
         * 构造方块状态生成器
         * 
         * @param output 输出包装器
         * @param exFileHelper 现有文件助手
         */
        public BlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, MOD_ID, exFileHelper);
        }

        /**
         * 注册方块状态和模型
         * <p>
         * 为末影箱和末影储罐创建简单的占位模型，
         * 使用黑曜石纹理作为粒子纹理。
         */
        @Override
        protected void registerStatesAndModels() {
            ModelFile model = models()
                    .withExistingParent("dummy", "block")
                    .texture("particle", "minecraft:block/obsidian");
            simpleBlock(ENDER_CHEST_BLOCK.get(), model);
            simpleBlock(ENDER_TANK_BLOCK.get(), model);
        }
    }

    /**
     * 方块标签生成器
     * <p>
     * 生成方块的数据标签，用于定义方块的特性和行为。
     * 主要用于指定哪些工具可以挖掘特定的方块。
     */
    private static class BlockTagGen extends BlockTagsProvider {

        /**
         * 构造方块标签生成器
         * 
         * @param output 输出包装器
         * @param lookupProvider 查找提供者
         * @param existingFileHelper 现有文件助手
         */
        public BlockTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, MOD_ID, existingFileHelper);
        }

        /**
         * 添加方块标签
         * <p>
         * 将末影箱和末影储罐添加到"可用镐子挖掘"的标签中，
         * 这样玩家就知道需要使用镐子来挖掘这些方块。
         * 
         * @param pProvider 提供者
         */
        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(ENDER_CHEST_BLOCK.get())
                    .add(ENDER_TANK_BLOCK.get());
        }
    }

    /**
     * 配方生成器
     * <p>
     * 生成所有末影存储物品的合成配方，包括：
     * - 基础合成配方：末影背包、末影箱、末影储罐
     * - 特殊配方：重新着色配方
     * <p>
     * 所有配方都使用自定义的CreateRecipe类型，支持频率设置功能。
     * 重新着色配方允许玩家改变现有物品的颜色频率。
     */
    private static class Recipes extends RecipeProvider {

        /**
         * 构造配方生成器
         * 
         * @param lookupProvider 查找提供者
         * @param output 输出包装器
         */
        public Recipes(CompletableFuture<HolderLookup.Provider> lookupProvider, PackOutput output) {
            super(lookupProvider, output, MOD_ID);
        }

        /**
         * 注册所有配方
         * <p>
         * 包括基础合成配方和特殊的重新着色配方。
         */
        @Override
        protected void registerRecipes() {
            // 末影背包合成配方
            customShaped(ENDER_POUCH, (group, category, pattern, stack, showNotification) -> new CreateRecipe(group, pattern, stack))
                    .key('P', Tags.Items.ENDER_PEARLS)    // 末影珍珠
                    .key('L', Tags.Items.LEATHERS)        // 皮革
                    .key('B', Items.BLAZE_POWDER)         // 烈焰粉
                    .key('W', CCLTags.Items.WOOLS)        // 羊毛
                    .patternLine("BLB")
                    .patternLine("LPL")
                    .patternLine("BWB");

            // 末影箱合成配方
            customShaped(ENDER_CHEST_ITEM, (group, category, pattern, stack, showNotification) -> new CreateRecipe(group, pattern, stack))
                    .key('P', Tags.Items.ENDER_PEARLS)    // 末影珍珠
                    .key('O', Tags.Items.OBSIDIANS)       // 黑曜石
                    .key('C', Tags.Items.CHESTS_WOODEN)   // 木箱子
                    .key('B', Items.BLAZE_ROD)            // 烈焰棒
                    .key('W', CCLTags.Items.WOOLS)        // 羊毛
                    .patternLine("BWB")
                    .patternLine("OCO")
                    .patternLine("BPB");
            
            // 末影储罐合成配方
            customShaped(ENDER_TANK_ITEM, (group, category, pattern, stack, showNotification) -> new CreateRecipe(group, pattern, stack))
                    .key('P', Tags.Items.ENDER_PEARLS)    // 末影珍珠
                    .key('O', Tags.Items.OBSIDIANS)       // 黑曜石
                    .key('C', Items.CAULDRON)             // 炼药锅
                    .key('B', Items.BLAZE_ROD)            // 烈焰棒
                    .key('W', CCLTags.Items.WOOLS)        // 羊毛
                    .patternLine("BWB")
                    .patternLine("OCO")
                    .patternLine("BPB");

            // 重新着色配方（特殊配方类型）
            special(ResourceLocation.fromNamespaceAndPath(MOD_ID, "recolour_ender_pouch"), () -> new ReColourRecipe(new ItemStack(ENDER_POUCH.get())));
            special(ResourceLocation.fromNamespaceAndPath(MOD_ID, "recolour_ender_chest"), () -> new ReColourRecipe(new ItemStack(ENDER_CHEST_ITEM.get())));
            special(ResourceLocation.fromNamespaceAndPath(MOD_ID, "recolour_ender_tank"), () -> new ReColourRecipe(new ItemStack(ENDER_TANK_ITEM.get())));
        }
    }
}
