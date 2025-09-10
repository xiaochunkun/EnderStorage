package codechicken.enderstorage.init;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.block.BlockEnderChest;
import codechicken.enderstorage.block.BlockEnderTank;
import codechicken.enderstorage.container.ContainerEnderItemStorage;
import codechicken.enderstorage.item.ItemEnderPouch;
import codechicken.enderstorage.item.ItemEnderStorage;
import codechicken.enderstorage.recipe.CreateRecipe;
import codechicken.enderstorage.recipe.ReColourRecipe;
import codechicken.enderstorage.tile.TileEnderChest;
import codechicken.enderstorage.tile.TileEnderTank;
import codechicken.lib.inventory.container.CCLMenuType;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static codechicken.enderstorage.EnderStorage.MOD_ID;

/**
 * 末影存储模组内容初始化类
 * 
 * 负责注册末影存储模组的所有游戏内容，包括：
 * - 方块和物品（末影箱、末影罐、末影袋）
 * - 方块实体类型
 * - 数据组件类型（频率数据）
 * - 菜单类型（GUI容器）
 * - 配方序列化器
 * - 能力系统集成
 * 
 * 使用DeferredRegister系统进行延迟注册，确保正确的初始化顺序。
 * 
 * @author covers1624
 * @since 29/10/19
 */
public class EnderStorageModContent {

    /** 初始化锁，防止重复初始化 */
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");
    
    // ===========================================
    // 延迟注册器定义
    // ===========================================
    
    /** 方块注册器 */
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    /** 物品注册器 */
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    /** 方块实体类型注册器 */
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    /** 数据组件类型注册器 */
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    /** 菜单类型注册器 */
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);
    /** 配方序列化器注册器 */
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);

    // ===========================================
    // 方块注册
    // ===========================================
    
    /** 方块属性配置 - 石色地图色，高硬度和抗爆性 */
    private static final BlockBehaviour.Properties blockProps = Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(20, 100);
    
    /** 末影箱方块 */
    public static final DeferredHolder<Block, BlockEnderChest> ENDER_CHEST_BLOCK = BLOCKS.register("ender_chest", () -> new BlockEnderChest(blockProps));
    /** 末影罐方块 */
    public static final DeferredHolder<Block, BlockEnderTank> ENDER_TANK_BLOCK = BLOCKS.register("ender_tank", () -> new BlockEnderTank(blockProps));

    // ===========================================
    // 物品注册
    // ===========================================
    
    /** 末影箱物品 */
    public static final DeferredHolder<Item, ItemEnderStorage> ENDER_CHEST_ITEM = ITEMS.register("ender_chest", () -> new ItemEnderStorage(ENDER_CHEST_BLOCK.get()));
    /** 末影罐物品 */
    public static final DeferredHolder<Item, ItemEnderStorage> ENDER_TANK_ITEM = ITEMS.register("ender_tank", () -> new ItemEnderStorage(ENDER_TANK_BLOCK.get()));
    /** 末影袋物品 */
    public static final DeferredHolder<Item, ItemEnderPouch> ENDER_POUCH = ITEMS.register("ender_pouch", ItemEnderPouch::new);

    // ===========================================
    // 方块实体类型注册
    // ===========================================
    
    /** 末影箱方块实体类型 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEnderChest>> ENDER_CHEST_TILE = BLOCK_ENTITY_TYPES.register("ender_chest", () ->
            BlockEntityType.Builder.of(TileEnderChest::new, ENDER_CHEST_BLOCK.get()).build(null)
    );
    /** 末影罐方块实体类型 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEnderTank>> ENDER_TANK_TILE = BLOCK_ENTITY_TYPES.register("ender_tank", () ->
            BlockEntityType.Builder.of(TileEnderTank::new, ENDER_TANK_BLOCK.get()).build(null)
    );

    // ===========================================
    // 数据组件类型注册
    // ===========================================
    
    /** 频率数据组件类型 - 用于存储末影存储设备的频率信息 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Frequency>> FREQUENCY_DATA_COMPONENT = DATA_COMPONENTS.register("frequency", () ->
            DataComponentType.<Frequency>builder()
                    .persistent(Frequency.CODEC)        // 持久化编解码器
                    .networkSynchronized(Frequency.STREAM_CODEC) // 网络同步编解码器
                    .build()
    );

    // ===========================================
    // 菜单类型注册
    // ===========================================
    
    /** 末影物品存储菜单类型 */
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerEnderItemStorage>> ENDER_ITEM_STORAGE = MENU_TYPES.register("ender_item_storage", () ->
            CCLMenuType.create(ContainerEnderItemStorage::new)
    );

    // ===========================================
    // 配方序列化器注册
    // ===========================================
    
    /** 创建配方序列化器 - 用于末影存储设备的创建配方 */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CreateRecipe>> CREATE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("create_recipe",
            CreateRecipe.Serializer::new
    );
    /** 重新染色配方序列化器 - 用于末影存储设备的染色配方 */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ReColourRecipe>> RECOLOUR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("recolour_recipe",
            ReColourRecipe.Serializer::new
    );

    /**
     * 初始化模组内容
     * 
     * 注册所有延迟注册器到模组事件总线，并设置事件监听器。
     * 这个方法应该在模组构造函数中调用。
     * 
     * @param modBus 模组事件总线
     * @throws IllegalStateException 如果已经初始化过
     */
    public static void init(IEventBus modBus) {
        LOCK.lock();
        
        // 注册所有延迟注册器
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        DATA_COMPONENTS.register(modBus);
        MENU_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        
        // 注册事件监听器
        modBus.addListener(EnderStorageModContent::onCreativeTabBuild);
        modBus.addListener(EnderStorageModContent::onRegisterCaps);
    }

    /**
     * 构建创造模式标签页内容事件处理
     * 
     * 将末影存储的物品添加到建筑方块创造模式标签页中。
     * 
     * @param event 构建创造模式标签页内容事件
     */
    private static void onCreativeTabBuild(BuildCreativeModeTabContentsEvent event) {
        // 只在建筑方块标签页中添加我们的物品
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ENDER_POUCH.get());      // 末影袋
            event.accept(ENDER_CHEST_BLOCK.get()); // 末影箱
            event.accept(ENDER_TANK_BLOCK.get());  // 末影罐
        }
    }

    /**
     * 注册能力系统事件处理
     * 
     * 为末影存储的方块实体注册相应的能力提供者：
     * - 末影箱注册物品处理能力
     * - 末影罐注册流体处理能力
     * 
     * @param event 注册能力事件
     */
    private static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        // 为末影箱注册物品处理能力
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ENDER_CHEST_TILE.get(), (object, context) -> object.getItemHandler());
        // 为末影罐注册流体处理能力  
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ENDER_TANK_TILE.get(), (object, context) -> object.getFluidHandler());
    }
}
