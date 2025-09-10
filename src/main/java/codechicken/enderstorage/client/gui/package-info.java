/**
 * 客户端 GUI 包 - 用户界面和交互实现
 * 
 * <p>
 * 本包包含了所有末影存储设备的客户端GUI实现，提供直观友好的
 * 用户交互界面，包括存储管理、频率设置和状态显示。
 * </p>
 * 
 * <h2>核心GUI类</h2>
 * 
 * <h3>主要界面实现</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.client.gui.GuiEnderItemStorage} - 末影物品存储GUI</li>
 * </ul>
 * 
 * <h2>GUI特性</h2>
 * 
 * <h3>末影物品存储GUI</h3>
 * <ul>
 *   <li><strong>存储显示</strong> - 27个存储槽位的直观展示</li>
 *   <li><strong>频率显示</strong> - 实时显示当前设备的频率颜色</li>
 *   <li><strong>状态指示</strong> - 显示私人/公共模式和所有者信息</li>
 *   <li><strong>操作提示</strong> - 提供友好的操作指导和提示</li>
 * </ul>
 * 
 * <h2>GUI设计</h2>
 * 
 * <h3>布局结构</h3>
 * <p>
 * GUI采用模块化设计，包括多个组件区域：
 * </p>
 * <ul>
 *   <li><strong>存储区域</strong> - 3x9格子的主存储显示区</li>
 *   <li><strong>玩家背包</strong> - 4x9格子的玩家背包区域</li>
 *   <li><strong>状态栏</strong> - 顶部的频率和状态显示区</li>
 *   <li><strong>控制栏</strong> - 底部的操作按钮和快捷键</li>
 * </ul>
 * 
 * <h3>视觉设计</h3>
 * <pre>{@code
 * // GUI渲染的核心方法
 * @Override
 * protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
 *     // 绘制背景纹理
 *     RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
 *     blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
 *     
 *     // 绘制频率颜色指示器
 *     renderFrequencyIndicators(poseStack, mouseX, mouseY);
 *     
 *     // 绘制状态指示器
 *     renderStatusIndicators(poseStack);
 * }
 * }</pre>
 * 
 * <h2>交互功能</h2>
 * 
 * <h3>鼠标交互</h3>
 * <p>
 * 支持多种鼠标交互方式：
 * </p>
 * <ul>
 *   <li><strong>左键点击</strong> - 正常的物品操作</li>
 *   <li><strong>右键点击</strong> - 快速移动半堆物品</li>
 *   <li><strong>中键点击</strong> - 复制物品堆栈(创造模式)</li>
 *   <li><strong>滚轮操作</strong> - 在频率显示区循环切换颜色</li>
 * </ul>
 * 
 * <h3>键盘快捷键</h3>
 * <pre>{@code
 * @Override
 * public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
 *     // 快速移动物品到背包/存储
 *     if (keyCode == GLFW.GLFW_KEY_SPACE) {
 *         quickMoveItems();
 *         return true;
 *     }
 *     
 *     // 打开频率设置界面
 *     if (keyCode == GLFW.GLFW_KEY_F) {
 *         openFrequencySettings();
 *         return true;
 *     }
 *     
 *     return super.keyPressed(keyCode, scanCode, modifiers);
 * }
 * }</pre>
 * 
 * <h2>状态管理</h2>
 * 
 * <h3>实时更新</h3>
 * <p>
 * GUI会实时响应存储状态的变化：
 * </p>
 * <ul>
 *   <li><strong>物品同步</strong> - 实时显示存储内容变化</li>
 *   <li><strong>频率更新</strong> - 实时更新频率颜色显示</li>
 *   <li><strong>权限检查</strong> - 动态检查和显示访问权限</li>
 *   <li><strong>连接状态</strong> - 显示与服务器的连接状态</li>
 * </ul>
 * 
 * <h2>本地化支持</h2>
 * 
 * <h3>多语言支持</h3>
 * <p>
 * GUI提供完整的本地化支持：
 * </p>
 * <ul>
 *   <li><strong>文本翻译</strong> - 所有界面文本都支持翻译</li>
 *   <li><strong>工具提示</strong> - 本地化的工具提示和帮助信息</li>
 *   <li><strong>格式化</strong> - 本地化的数字和日期格式</li>
 * </ul>
 * 
 * <h2>辅助功能</h2>
 * 
 * <h3>可访问性</h3>
 * <p>
 * GUI考虑了可访问性设计：
 * </p>
 * <ul>
 *   <li><strong>键盘导航</strong> - 完整的键盘导航支持</li>
 *   <li><strong>屏幕阅读器</strong> - 兼容屏幕阅读器</li>
 *   <li><strong>高对比度</strong> - 支持高对比度显示模式</li>
 * </ul>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.container
 * @see codechicken.enderstorage.client
 */
@NonNullApi
package codechicken.enderstorage.client.gui;

import net.covers1624.quack.annotation.NonNullApi;
