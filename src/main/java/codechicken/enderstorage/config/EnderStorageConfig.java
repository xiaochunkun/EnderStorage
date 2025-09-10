package codechicken.enderstorage.config;

import codechicken.lib.config.ConfigCategory;
import codechicken.lib.config.ConfigFile;
import codechicken.lib.config.ConfigValue;
import com.mojang.logging.LogUtils;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Paths;

import static codechicken.enderstorage.EnderStorage.MOD_ID;
import static java.util.Objects.requireNonNull;

/**
 * 末影存储配置管理类
 * 
 * 负责加载和管理末影存储模组的所有配置项，包括：
 * - 个人物品设置（用于锁定末影箱/罐）
 * - 无政府模式开关
 * - 存储大小配置
 * - 视觉效果设置
 * - 音效配置
 * 
 * 配置文件位置：./config/EnderStorage.cfg
 * 
 * @author covers1624
 * @since 28/10/19
 */
public class EnderStorageConfig {

    /** 日志记录器 */
    private static final Logger LOGGER = LogUtils.getLogger();
    /** 初始化锁，防止重复初始化 */
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    /** 个人物品标签配置值（用于锁定末影箱/罐的物品注册名） */
    private static @Nullable ConfigValue personalItemTag;
    /** 个人物品堆叠（缓存的物品实例） */
    @Nullable
    private static ItemStack personalItem;
    /** 无政府模式开关 - 为true时箱子失去个人设置并在破坏时掉落钻石 */
    public static boolean anarchyMode;
    /** 存储大小配置 - 0=3x3, 1=3x9, 2=6x9 */
    public static int storageSize;

    /** 禁用创建者视觉效果 - 为true时禁用创建者头上的罐子显示 */
    public static boolean disableCreatorVisuals;
    /** 使用原版末影箱音效 - 为true时使用原版末影箱音效而非标准箱子音效 */
    public static boolean useVanillaEnderChestSounds;

    /**
     * 加载配置文件
     * 
     * 从配置文件中读取所有配置项并初始化静态字段。
     * 配置文件路径：./config/EnderStorage.cfg
     * 
     * @throws IllegalStateException 如果已经初始化过
     */
    public static void load() {
        LOCK.lock();

        // 创建并加载配置文件
        ConfigCategory config = new ConfigFile(MOD_ID)
                .path(Paths.get("./config/EnderStorage.cfg"))
                .load();
        // TODO: 配置同步管理器注册（暂时禁用）
//        ConfigSyncManager.registerSync(new ResourceLocation("enderstorage:config"), config);
        // 配置个人物品（用于锁定末影箱和罐的物品）
        personalItemTag = config.getValue("personalItem")
                .setComment("The RegistryName for the Item to lock EnderChests and Tanks.")
                .setDefaultString("minecraft:diamond");
        // 配置无政府模式
        anarchyMode = config.getValue("anarchyMode")
                .setComment("Causes chests to lose personal settings and drop the diamond on break.")
                .setDefaultBoolean(false)
                .getBoolean();
        // 配置存储大小
        storageSize = config.getValue("item_storage_size")
                .setComment("The size of each inventory of EnderStorage, 0 = 3x3, 1 = 3x9, 2 = 6x9, default = 1")
                .setDefaultInt(1)
                .getInt();

        // 配置创建者视觉效果
        disableCreatorVisuals = config.getValue("disableCreatorVisuals")
                .setComment("Disables the tank on top of creators heads.")
                .setDefaultBoolean(false)
                .getBoolean();
        // 配置音效设置
        useVanillaEnderChestSounds = config.getValue("useVanillaEnderChestsSounds")
                .setComment("Enable this to make EnderStorage use vanilla's EnderChest sounds instead of the standard chest.")
                .setDefaultBoolean(false)
                .getBoolean();
        // 保存配置文件
        config.save();
    }

    /**
     * 获取个人物品堆叠
     * 
     * 用于锁定末影箱和末影罐的物品。首次调用时会根据配置的注册名
     * 创建物品实例并缓存，后续调用直接返回缓存的实例。
     * 
     * @return 个人物品堆叠，默认为钻石
     * @throws NullPointerException 如果配置未初始化
     */
    public static ItemStack getPersonalItem() {
        requireNonNull(personalItemTag);

        // 懒加载个人物品实例
        if (personalItem == null) {
            // 根据注册名获取物品
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(personalItemTag.getString()));
            // 验证物品有效性，无效时重置为默认值
            if (item == Items.AIR) {
                LOGGER.error("Invalid personal item in config. Got: '{}. Resetting to default.", personalItemTag.getString());
                item = Items.DIAMOND;
                personalItemTag.reset();
                personalItemTag.save();
            }
            // 创建并缓存物品堆叠
            personalItem = new ItemStack(item);
        }
        return personalItem;
    }
}
