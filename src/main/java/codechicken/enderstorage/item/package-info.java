/**
 * 物品管理包 - 末影存储相关物品实现
 * 
 * <p>
 * 本包包含了所有末影存储相关的物品实现，包括方块物品和便携式存储物品。
 * 这些物品提供了与末影存储系统交互的所有功能。
 * </p>
 * 
 * <h2>核心物品类</h2>
 * 
 * <h3>主要物品实现</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.item.ItemEnderStorage} - 末影存储方块物品的通用基类</li>
 *   <li>{@link codechicken.enderstorage.item.ItemEnderPouch} - 末影袋物品实现</li>
 * </ul>
 * 
 * <h2>物品特性</h2>
 * 
 * <h3>末影存储方块物品</h3>
 * <ul>
 *   <li><strong>频率保持</strong> - 在物品堆栈中保存频率信息</li>
 *   <li><strong>显示信息</strong> - 在工具提示中显示频率和状态</li>
 *   <li><strong>放置逻辑</strong> - 保持频率在放置时不丢失</li>
 *   <li><strong>验证功能</strong> - 验证放置位置和条件</li>
 * </ul>
 * 
 * <h3>末影袋 (Ender Pouch)</h3>
 * <ul>
 *   <li><strong>便携存储</strong> - 可随身携带的末影箱子访问</li>
 *   <li><strong>频率同步</strong> - 与对应频率的末影箱子共享存储</li>
 *   <li><strong>右键使用</strong> - 右键打开存储界面</li>
 *   <li><strong>修理机制</strong> - 支持通过铁砧修理损坏</li>
 * </ul>
 * 
 * <h2>物品实现细节</h2>
 * 
 * <h3>NBT 数据管理</h3>
 * <p>
 * 物品使用 NBT 数据存储持久化信息：
 * </p>
 * <pre>{@code
 * // 保存频率信息
 * CompoundTag tag = stack.getOrCreateTag();
 * tag.putInt("frequency", frequency.hashCode());
 * tag.putIntArray("colors", frequency.toIntArray());
 * 
 * // 保存所有者信息
 * if (owner != null) {
 *     tag.putUUID("owner", owner);
 *     tag.putString("ownerName", ownerName);
 * }
 * }</pre>
 * 
 * <h3>工具提示</h3>
 * <p>
 * 物品提供丰富的工具提示信息：
 * </p>
 * <pre>{@code
 * @Override
 * public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
 *     Frequency frequency = getFrequency(stack);
 *     if (frequency != null) {
 *         tooltip.add(Component.translatable("enderstorage.tooltip.frequency")
 *             .append(": ").append(frequency.getDisplayName()));
 *         
 *         if (isPrivate(stack)) {
 *             tooltip.add(Component.translatable("enderstorage.tooltip.private"));
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h2>交互机制</h2>
 * 
 * <h3>右键使用</h3>
 * <p>
 * 末影袋的使用逻辑：
 * </p>
 * <pre>{@code
 * @Override
 * public InteractionResult use(Level world, Player player, InteractionHand hand) {
 *     ItemStack stack = player.getItemInHand(hand);
 *     Frequency frequency = getFrequency(stack);
 *     
 *     if (frequency != null && !world.isClientSide) {
 *         // 获取对应的存储实例
 *         AbstractEnderStorage storage = EnderStorageManager.instance(false)
 *             .getStorage(frequency, StorageType.ITEMS);
 *         
 *         // 打开存储界面
 *         player.openMenu(new EnderPouchMenuProvider(storage));
 *     }
 *     
 *     return InteractionResult.SUCCESS;
 * }
 * }</pre>
 * 
 * <h3>频率管理</h3>
 * <p>
 * 频率可以通过多种方式修改：
 * </p>
 * <ul>
 *   <li><strong>染料修改</strong> - 使用染料右键点击修改频率</li>
 *   <li><strong>工作台重设</strong> - 在工作台中重新合成重置频率</li>
 *   <li><strong>指令设置</strong> - 通过指令直接设置频率</li>
 * </ul>
 * 
 * <h2>兼容性支持</h2>
 * 
 * <h3>模组集成</h3>
 * <p>
 * 物品与其他模组的集成支持：
 * </p>
 * <ul>
 *   <li><strong>JEI 集成</strong> - 显示合成配方和使用方法</li>
 *   <li><strong>WAILA 支持</strong> - 显示物品详细信息</li>
 *   <li><strong>背包支持</strong> - 兼容各种背包模组</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.block
 * @see codechicken.enderstorage.api
 */
@NonNullApi
package codechicken.enderstorage.item;

import net.covers1624.quack.annotation.NonNullApi;
