/**
 * 配置管理包 - 模组配置选项和设置管理
 * 
 * <p>
 * 本包负责管理 EnderStorage 模组的所有配置选项，包括服务器端配置、
 * 客户端配置以及运行时配置的动态调整。
 * </p>
 * 
 * <h2>核心配置类</h2>
 * 
 * <h3>主要组件</h3>
 * <ul>
 *   <li>{@link codechicken.enderstorage.config.EnderStorageConfig} - 主配置管理器</li>
 * </ul>
 * 
 * <h2>配置类别</h2>
 * 
 * <h3>服务器端配置</h3>
 * <ul>
 *   <li><strong>存储限制</strong> - 每个频率的最大存储容量</li>
 *   <li><strong>访问控制</strong> - 私人模式和权限设置</li>
 *   <li><strong>性能设置</strong> - 同步频率和缓存策略</li>
 *   <li><strong>安全选项</strong> - 防作弊和数据保护</li>
 * </ul>
 * 
 * <h3>客户端配置</h3>
 * <ul>
 *   <li><strong>渲染选项</strong> - 视觉效果和性能设置</li>
 *   <li><strong>GUI设置</strong> - 界面布局和显示选项</li>
 *   <li><strong>音效配置</strong> - 声音效果的开关和音量</li>
 * </ul>
 * 
 * <h2>配置特性</h2>
 * 
 * <h3>动态配置</h3>
 * <p>
 * 支持运行时配置修改，无需重启服务器或客户端：
 * </p>
 * <pre>{@code
 * // 动态修改配置
 * EnderStorageConfig.setMaxStorageSize(54);
 * EnderStorageConfig.setRenderDistance(128);
 * 
 * // 配置同步到客户端
 * EnderStorageConfig.syncToClients();
 * }</pre>
 * 
 * <h3>配置验证</h3>
 * <p>
 * 所有配置项都包含验证逻辑，确保配置值的合法性和安全性。
 * </p>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 获取配置值
 * int maxSize = EnderStorageConfig.getMaxStorageSize();
 * boolean allowPrivate = EnderStorageConfig.isPrivateModeEnabled();
 * 
 * // 注册配置监听器
 * EnderStorageConfig.addConfigListener(config -> {
 *     // 配置变更时的回调处理
 * });
 * }</pre>
 * 
 * @author CodeChicken
 * @author covers1624
 * @since 1.0.0
 * @version 2.9.4
 * @see codechicken.enderstorage.EnderStorage
 */
@NonNullApi
package codechicken.enderstorage.config;

import net.covers1624.quack.annotation.NonNullApi;
