/**
 * 自定义配方包 - 末影存储相关的合成和变色配方
 * 
 * <p>
 * 本包定义了 EnderStorage 模组的所有自定义配方，包括末影存储设备的
 * 合成配方和频率变色配方，为玩家提供灵活的制作方式。
 * </p>
 * 
 * <h2>核心配方类</h2>
 * 
 * <h3>主要配方实现</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.recipe.CreateRecipe} - 末影存储设备创建配方</li>
 *   <li>{@link codechicken.enderstorage.recipe.ReColourRecipe} - 频率颜色变更配方</li>
 * </ul>
 * 
 * <h2>配方类型</h2>
 * 
 * <h3>创建配方</h3>
 * <ul>
 *   <li><strong>末影箱子</strong> - 使用末影珍珠、箱子和染料合成</li>
 *   <li><strong>末影储罐</strong> - 使用末影珍珠、炧钜桶和染料合成</li>
 *   <li><strong>末影袋</strong> - 使用末影珍珠、皮革和染料合成</li>
 * </ul>
 * 
 * <h3>变色配方</h3>
 * <ul>
 *   <li><strong>频率修改</strong> - 使用染料修改设备频率</li>
 *   <li><strong>多次变色</strong> - 支持多次变更同一设备的颜色</li>
 *   <li><strong>数据保持</strong> - 变色后保持原有存储内容</li>
 * </ul>
 * 
 * <h2>配方特性</h2>
 * 
 * <h3>智能识别</h3>
 * <ul>
 *   <li><strong>NBT保持</strong> - 保留物品的NBT数据</li>
 *   <li><strong>损坏值保持</strong> - 保持工具的损坏值</li>
 *   <li><strong>附魔保持</strong> - 保持物品的附魔属性</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.item
 * @see codechicken.enderstorage.block
 */
@NonNullApi
package codechicken.enderstorage.recipe;

import net.covers1624.quack.annotation.NonNullApi;
