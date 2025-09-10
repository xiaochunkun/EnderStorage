package codechicken.enderstorage.init;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.client.Shaders;
import codechicken.enderstorage.client.gui.GuiEnderItemStorage;
import codechicken.enderstorage.client.render.entity.TankLayerRenderer;
import codechicken.enderstorage.client.render.tile.RenderTileEnderChest;
import codechicken.enderstorage.client.render.tile.RenderTileEnderTank;
import codechicken.enderstorage.config.EnderStorageConfig;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static codechicken.enderstorage.EnderStorage.MOD_ID;
import static codechicken.enderstorage.init.EnderStorageModContent.*;

/**
 * 客户端初始化类
 * <p>
 * 负责初始化所有客户端相关的功能，包括：
 * - 注册渲染器（方块实体渲染器）
 * - 注册GUI界面
 * - 添加玩家皮肤渲染层
 * - 注册物品属性谓词
 * - 初始化着色器系统
 * <p>
 * 此类在客户端侧运行，确保所有的客户端专用功能能够正确初始化和运行。
 * 
 * @author covers1624 创建于 2022年6月4日
 */
public class ClientInit {

    /** 防止重复初始化的崩溃锁 */
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    /**
     * 客户端初始化入口方法
     * <p>
     * 注册所有必要的客户端事件监听器，包括：
     * - 渲染器注册事件
     * - 渲染层添加事件  
     * - 菜单界面注册事件
     * - 客户端设置事件
     * <p>
     * 同时初始化着色器系统。
     * 
     * @param modBus 模组事件总线，用于注册事件监听器
     */
    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(ClientInit::onRegisterRenderers);
        modBus.addListener(ClientInit::onAddRenderLayers);
        modBus.addListener(ClientInit::onRegisterMenuScreens);
        modBus.addListener(ClientInit::onClientSetupEvent);
        Shaders.init(modBus);
    }

    /**
     * 注册方块实体渲染器事件处理方法
     * <p>
     * 为末影箱和末影储罐方块实体注册相应的渲染器，
     * 使得这些方块在游戏中能够正确显示其3D模型和动画效果。
     * 
     * @param event 渲染器注册事件
     */
    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRenderers.register(ENDER_CHEST_TILE.get(), RenderTileEnderChest::new);
        BlockEntityRenderers.register(ENDER_TANK_TILE.get(), RenderTileEnderTank::new);
    }

    /**
     * 添加玩家渲染层事件处理方法
     * <p>
     * 为所有玩家皮肤模型添加储罐渲染层，用于显示开发者特殊效果。
     * 此功能可以通过配置文件中的 disableCreatorVisuals 选项来禁用。
     * <p>
     * 该渲染层会在玩家身上显示特殊的视觉效果，通常用于识别模组的创建者。
     * 
     * @param event 渲染层添加事件
     */
    @SuppressWarnings ({ "rawtypes", "unchecked" })
    private static void onAddRenderLayers(EntityRenderersEvent.AddLayers event) {
        if (!EnderStorageConfig.disableCreatorVisuals) {
            for (PlayerSkin.Model skin : event.getSkins()) {
                var skinRenderer = (LivingEntityRenderer) event.getSkin(skin);
                assert skinRenderer != null;
                skinRenderer.addLayer(new TankLayerRenderer(skinRenderer));
            }
        }
    }

    /**
     * 注册菜单界面事件处理方法
     * <p>
     * 为末影物品存储容器注册对应的GUI界面，
     * 使得玩家可以通过图形界面与末影背包进行交互。
     * 
     * @param event 菜单界面注册事件
     */
    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ENDER_ITEM_STORAGE.get(), GuiEnderItemStorage::new);
    }

    /**
     * 客户端设置事件处理方法
     * <p>
     * 在客户端设置阶段注册物品属性谓词。
     * 使用 enqueueWork 确保在正确的线程中执行注册操作。
     * 
     * @param event 客户端设置事件
     */
    private static void onClientSetupEvent(FMLClientSetupEvent event) {
        event.enqueueWork(ClientInit::registerPredicates);
    }

    /**
     * 注册物品属性谓词
     * <p>
     * 为末影背包物品注册多个属性谓词，用于控制物品模型的显示：
     * - owned: 背包是否有所有者（0或1）
     * - open: 背包当前的打开计数
     * - left: 左侧颜色代码（0-15）
     * - middle: 中间颜色代码（0-15）
     * - right: 右侧颜色代码（0-15）
     * <p>
     * 这些属性谓词用于动态改变末影背包的纹理和模型，
     * 以反映其当前的状态和频率设置。
     */
    private static void registerPredicates() {
        // 注册"owned"属性谓词：检查背包是否有所有者
        ItemProperties.register(
                ENDER_POUCH.get(),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "owned"),
                (ClampedItemPropertyFunction) (pStack, pLevel, pEntity, pSeed) -> Frequency.readFromStack(pStack).hasOwner() ? 1 : 0
        );
        // 注册"open"属性谓词：获取背包当前的打开计数
        ItemProperties.register(
                ENDER_POUCH.get(),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "open"),
                (ClampedItemPropertyFunction) (pStack, pLevel, pEntity, pSeed) -> EnderStorageManager.instance(true).getStorage(Frequency.readFromStack(pStack), EnderItemStorage.TYPE).openCount()
        );
        // 注册"left"属性谓词：获取左侧颜色代码
        ItemProperties.register(
                ENDER_POUCH.get(),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "left"),
                (pStack, pLevel, pEntity, pSeed) -> Frequency.readFromStack(pStack).left().ordinal()
        );
        // 注册"middle"属性谓词：获取中间颜色代码
        ItemProperties.register(
                ENDER_POUCH.get(),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "middle"),
                (pStack, pLevel, pEntity, pSeed) -> Frequency.readFromStack(pStack).middle().ordinal()
        );
        // 注册"right"属性谓词：获取右侧颜色代码
        ItemProperties.register(
                ENDER_POUCH.get(),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "right"),
                (pStack, pLevel, pEntity, pSeed) -> Frequency.readFromStack(pStack).right().ordinal()
        );
    }
}
