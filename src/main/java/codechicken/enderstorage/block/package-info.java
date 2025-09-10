/**
 * 末影存储方块包 - 游戏世界中的物理方块实现
 * 
 * <p>
 * 本包包含所有末影存储相关方块的实现类，包括末影箱子和末影储罐。
 * 这些方块类负责定义方块的行为、属性、交互逻辑以及与游戏世界的集成。
 * </p>
 * 
 * <h2>核心方块类</h2>
 * 
 * <h3>主要方块实现</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.block.BlockEnderChest} - 末影箱子方块实现</li>
 *   <li>{@link codechicken.enderstorage.block.BlockEnderTank} - 末影储罐方块实现</li>
 *   <li>{@link codechicken.enderstorage.block.BlockEnderStorage} - 末影存储方块的通用基类</li>
 * </ul>
 * 
 * <h2>方块特性</h2>
 * 
 * <h3>末影箱子 (Ender Chest)</h3>
 * <ul>
 *   <li><strong>物品存储</strong> - 提供27个物品槽位的存储空间</li>
 *   <li><strong>颜色频率</strong> - 通过右键点击改变三个颜色标识</li>
 *   <li><strong>跨维度同步</strong> - 相同频率的箱子共享存储内容</li>
 *   <li><strong>安全性</strong> - 支持私人模式，防止他人访问</li>
 *   <li><strong>GUI界面</strong> - 提供友好的用户交互界面</li>
 * </ul>
 * 
 * <h3>末影储罐 (Ender Tank)</h3>
 * <ul>
 *   <li><strong>流体存储</strong> - 存储各种游戏中的流体</li>
 *   <li><strong>容量显示</strong> - 通过视觉效果显示当前存储量</li>
 *   <li><strong>输入输出</strong> - 支持管道和其他设备的流体传输</li>
 *   <li><strong>红石比较器</strong> - 输出与存储量成比例的红石信号</li>
 *   <li><strong>自动化兼容</strong> - 与各种流体传输系统兼容</li>
 * </ul>
 * 
 * <h2>方块行为</h2>
 * 
 * <h3>放置和破坏</h3>
 * <pre>{@code
 * // 方块放置时会保留颜色频率信息
 * @Override
 * public void setPlacedBy(Level world, BlockPos pos, BlockState state, 
 *                        LivingEntity placer, ItemStack stack) {
 *     // 从物品堆栈恢复频率信息
 *     // 初始化方块实体数据
 * }
 * 
 * // 破坏时保存频率到物品
 * @Override
 * public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
 *     // 将频率信息保存到掉落物品中
 * }
 * }</pre>
 * 
 * <h3>玩家交互</h3>
 * <pre>{@code
 * // 右键交互处理
 * @Override
 * public InteractionResult use(BlockState state, Level world, BlockPos pos,
 *                             Player player, InteractionHand hand, BlockHitResult hit) {
 *     // 空手右键：打开存储界面
 *     // 持染料右键：改变颜色频率
 *     // 持扳手右键：切换私人/公共模式
 * }
 * }</pre>
 * 
 * <h2>频率管理</h2>
 * <p>
 * 方块的颜色频率通过三个颜色标识来定义，每个标识可以是16种染料颜色中的任意一种。
 * 玩家可以通过右键点击对应的颜色区域来循环切换颜色。
 * </p>
 * 
 * <h3>颜色切换机制</h3>
 * <ul>
 *   <li><strong>空手点击</strong> - 按顺序循环颜色</li>
 *   <li><strong>持染料点击</strong> - 直接设置为对应颜色</li>
 *   <li><strong>Shift+点击</strong> - 反向循环颜色</li>
 * </ul>
 * 
 * <h2>渲染集成</h2>
 * <p>
 * 方块类与客户端渲染系统紧密集成，支持：
 * </p>
 * <ul>
 *   <li><strong>动态颜色</strong> - 实时显示当前频率颜色</li>
 *   <li><strong>3D模型</strong> - 复杂的三维方块模型</li>
 *   <li><strong>动画效果</strong> - 开关动画和粒子效果</li>
 *   <li><strong>流体渲染</strong> - 储罐中流体的视觉显示</li>
 * </ul>
 * 
 * <h2>数据持久化</h2>
 * <p>
 * 方块的状态数据通过对应的 TileEntity 进行持久化，包括：
 * </p>
 * <ul>
 *   <li>颜色频率信息</li>
 *   <li>私人/公共模式设置</li>
 *   <li>所有者信息</li>
 *   <li>方块特定的配置数据</li>
 * </ul>
 * 
 * <h2>兼容性支持</h2>
 * <p>
 * 方块实现遵循 Minecraft 和 Forge/NeoForge 的标准接口，确保与其他模组的兼容性：
 * </p>
 * <ul>
 *   <li><strong>IItemHandler</strong> - 物品处理接口</li>
 *   <li><strong>IFluidHandler</strong> - 流体处理接口</li>
 *   <li><strong>Redstone Integration</strong> - 红石信号集成</li>
 *   <li><strong>Wrench Support</strong> - 扳手工具支持</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.tile
 * @see codechicken.enderstorage.client.render
 * @see codechicken.enderstorage.api
 */
@NonNullApi
package codechicken.enderstorage.block;

import net.covers1624.quack.annotation.NonNullApi;
