/**
 * 模组初始化包 - 模组内容注册和初始化管理
 * 
 * <p>
 * 本包负责 EnderStorage 模组的所有初始化工作，包括方块、物品、实体、
 * 配方等游戏内容的注册，以及客户端和服务器端的特定初始化逻辑。
 * </p>
 * 
 * <h2>初始化组件</h2>
 * 
 * <h3>主要初始化类</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.init.EnderStorageModContent} - 模组内容注册管理器</li>
 *   <li>{@link codechicken.enderstorage.init.ClientInit} - 客户端特定初始化</li>
 *   <li>{@link codechicken.enderstorage.init.DataGenerators} - 数据生成器注册</li>
 * </ul>
 * 
 * <h2>注册管理</h2>
 * 
 * <h3>游戏内容注册</h3>
 * <p>
 * 模组内容注册管理器负责注册所有游戏元素：
 * </p>
 * <ul>
 *   <li><strong>方块注册</strong> - 末影箱子和末影储罐方块</li>
 *   <li><strong>物品注册</strong> - 方块物品和末影袋物品</li>
 *   <li><strong>实体注册</strong> - TileEntity 和 BlockEntity 类型</li>
 *   <li><strong>容器注册</strong> - GUI 容器类型</li>
 *   <li><strong>配方注册</strong> - 自定义合成和变色配方</li>
 * </ul>
 * 
 * <h3>注册流程</h3>
 * <pre>{@code
 * // 1. 方块注册
 * public static final DeferredRegister<Block> BLOCKS = 
 *     DeferredRegister.create(ForgeRegistries.BLOCKS, EnderStorage.MOD_ID);
 * 
 * public static final RegistryObject<Block> ENDER_CHEST = 
 *     BLOCKS.register("ender_chest", () -> new BlockEnderChest());
 * 
 * // 2. 物品注册
 * public static final DeferredRegister<Item> ITEMS = 
 *     DeferredRegister.create(ForgeRegistries.ITEMS, EnderStorage.MOD_ID);
 * 
 * public static final RegistryObject<Item> ENDER_CHEST_ITEM = 
 *     ITEMS.register("ender_chest", () -> new BlockItem(ENDER_CHEST.get()));
 * }</pre>
 * 
 * <h2>客户端初始化</h2>
 * 
 * <h3>渲染注册</h3>
 * <p>
 * 客户端初始化负责注册所有渲染相关组件：
 * </p>
 * <ul>
 *   <li><strong>方块渲染器</strong> - TileEntity 特殊渲染器</li>
 *   <li><strong>物品渲染器</strong> - 物品栈特殊渲染器</li>
 *   <li><strong>屏幕注册</strong> - GUI 屏幕类型映射</li>
 *   <li><strong>材质加载</strong> - 着色器和材质资源</li>
 * </ul>
 * 
 * <h3>事件监听</h3>
 * <pre>{@code
 * @Mod.EventBusSubscriber(modid = EnderStorage.MOD_ID, value = Dist.CLIENT)
 * public class ClientInit {
 *     
 *     @SubscribeEvent
 *     public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
 *         event.registerBlockEntityRenderer(ENDER_CHEST_TILE.get(), RenderTileEnderChest::new);
 *         event.registerBlockEntityRenderer(ENDER_TANK_TILE.get(), RenderTileEnderTank::new);
 *     }
 *     
 *     @SubscribeEvent
 *     public static void onRegisterScreens(RegisterMenuScreensEvent event) {
 *         event.register(ENDER_CHEST_CONTAINER.get(), GuiEnderItemStorage::new);
 *     }
 * }
 * }</pre>
 * 
 * <h2>数据生成</h2>
 * 
 * <h3>游戏数据生成</h3>
 * <p>
 * 数据生成器负责自动生成游戏数据：
 * </p>
 * <ul>
 *   <li><strong>方块状态</strong> - 方块状态JSON文件</li>
 *   <li><strong>物品模型</strong> - 物品模型JSON文件</li>
 *   <li><strong>战利品表</strong> - 方块掉落物表</li>
 *   <li><strong>标签数据</strong> - 物品和方块标签</li>
 *   <li><strong>配方数据</strong> - 合成和熬烧配方</li>
 * </ul>
 * 
 * <h2>初始化顺序</h2>
 * <p>
 * 模组初始化遵循以下顺序：
 * </p>
 * <ol>
 *   <li>核心类型注册（Block, Item, TileEntity）</li>
 *   <li>游戏内容注册（配方、战利品表）</li>
 *   <li>客户端特定初始化（渲染器、GUI）</li>
 *   <li>网络和事件初始化</li>
 *   <li>数据生成器初始化</li>
 * </ol>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.EnderStorage
 * @see codechicken.enderstorage.client
 */
@NonNullApi
package codechicken.enderstorage.init;

import net.covers1624.quack.annotation.NonNullApi;
