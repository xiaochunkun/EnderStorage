package codechicken.enderstorage.container;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.lib.data.MCDataInput;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 末影物品存储容器类 - 末影箱GUI的后端逻辑
 * 
 * <p>此类管理末影物品存储的GUI界面逻辑，包括槽位布局、物品转移、玩家交互验证等。
 * 支持三种不同大小的存储容器（小、中、大），每种都有不同的槽位布局和容量。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li><strong>动态槽位布局</strong>：根据存储大小自动调整槽位排列</li>
 *   <li><strong>智能物品转移</strong>：支持Shift+点击快速转移物品</li>
 *   <li><strong>网络同步</strong>：从网络数据包构建容器状态</li>
 *   <li><strong>访问控制</strong>：验证玩家的访问权限</li>
 * </ul>
 * 
 * <h3>存储尺寸支持：</h3>
 * <ul>
 *   <li><strong>小型存储 (尺寸0)</strong>：3x3 = 9个槽位，类似工作台界面</li>
 *   <li><strong>中型存储 (尺寸1)</strong>：9x3 = 27个槽位，类似单箱子界面</li>
 *   <li><strong>大型存储 (尺寸2)</strong>：9x6 = 54个槽位，类似双箱子界面</li>
 * </ul>
 * 
 * <h3>GUI布局设计：</h3>
 * <p>所有尺寸的容器都包含：</p>
 * <ul>
 *   <li>末影存储区域：上方的存储槽位</li>
 *   <li>玩家物品栏：下方的36个槽位（27个背包+9个快捷栏）</li>
 *   <li>智能间距：根据存储大小自动调整垂直间距</li>
 * </ul>
 * 
 * <h3>网络同步机制：</h3>
 * <p>支持两种构建方式：</p>
 * <ul>
 *   <li>直接构建：使用已有的存储实例（服务器端）</li>
 *   <li>网络构建：从数据包解析频率和状态（客户端）</li>
 * </ul>
 * 
 * @author ChickenBones
 * @since 1.0.0
 * @see AbstractContainerMenu
 * @see EnderItemStorage
 */
public class ContainerEnderItemStorage extends AbstractContainerMenu {

    /** 关联的末影物品存储实例 */
    public EnderItemStorage chestInv;

    /**
     * 网络构造函数 - 从数据包创建容器（客户端使用）
     * 
     * <p>此构造函数用于客户端接收服务器发送的GUI打开数据包时使用。
     * 从数据包中解析频率信息，获取对应的存储实例，并处理额外的同步数据。</p>
     * 
     * @param windowId GUI窗口ID，用于网络同步
     * @param playerInv 玩家物品栏
     * @param packet 网络数据包，包含频率和状态信息
     */
    public ContainerEnderItemStorage(int windowId, Inventory playerInv, MCDataInput packet) {
        this(windowId, playerInv, EnderStorageManager.instance(true).getStorage(Frequency.readFromPacket(packet), EnderItemStorage.TYPE));
        chestInv.handleContainerPacket(packet); // 处理额外的容器同步数据
    }

    /**
     * 直接构造函数 - 使用现有存储实例创建容器（服务器使用）
     * 
     * <p>此构造函数用于服务器端直接打开存储界面时使用。根据存储的大小
     * 动态创建相应的槽位布局，并添加玩家物品栏槽位。</p>
     * 
     * @param windowId GUI窗口ID
     * @param playerInv 玩家物品栏
     * @param chestInv 末影存储实例
     */
    public ContainerEnderItemStorage(int windowId, Inventory playerInv, EnderItemStorage chestInv) {
        super(EnderStorageModContent.ENDER_ITEM_STORAGE.get(), windowId);
        this.chestInv = chestInv;
        chestInv.openInventory(); // 标记存储为已打开状态

        // 根据存储大小创建不同的槽位布局
        switch (chestInv.getSize()) {
            case 0: // 小型存储：3x3布局
                for (int row = 0; row < 3; ++row) {
                    for (int col = 0; col < 3; ++col) {
                        addSlot(new Slot(chestInv, col + row * 3, 62 + col * 18, 17 + row * 18));
                    }
                }
                addPlayerSlots(playerInv, 84); // 玩家槽位Y偏移84
                break;
            case 1: // 中型存储：9x3布局
                for (int row = 0; row < 3; ++row) {
                    for (int col = 0; col < 9; ++col) {
                        addSlot(new Slot(chestInv, col + row * 9, 8 + col * 18, 18 + row * 18));
                    }
                }
                addPlayerSlots(playerInv, 85); // 玩家槽位Y偏移85
                break;
            case 2: // 大型存储：9x6布局
                for (int row = 0; row < 6; ++row) {
                    for (int col = 0; col < 9; ++col) {
                        addSlot(new Slot(chestInv, col + row * 9, 8 + col * 18, 18 + row * 18));
                    }
                }
                addPlayerSlots(playerInv, 140); // 玩家槽位Y偏移140
                break;
        }
    }

    /**
     * 添加玩家物品栏槽位
     * 
     * <p>为容器添加标准的玩家物品栏槽位，包括27个背包槽位和9个快捷栏槽位。
     * 布局采用标准Minecraft容器设计。</p>
     * 
     * @param invplayer 玩家物品栏容器
     * @param yOffset Y轴偏移，用于适应不同大小的存储界面
     */
    private void addPlayerSlots(Container invplayer, int yOffset) {
        // 添加3x9的背包主区域槽位
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(invplayer, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
            }
        }

        // 添加1x9的快捷栏槽位
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(invplayer, col, 8 + col * 18, yOffset + 58));
        }
    }

    /**
     * 验证容器是否仍然有效
     * 
     * <p>检查玩家是否仍然可以访问此容器。委托给存储实例进行访问权限验证，
     * 包括距离检查、权限检查等。</p>
     * 
     * @param entityplayer 要验证的玩家
     * @return 如果玩家可以继续使用此容器则返回true
     */
    @Override
    public boolean stillValid(Player entityplayer) {
        return chestInv.stillValid(entityplayer);
    }

    /**
     * 快速转移物品堆栈（Shift+点击实现）
     * 
     * <p>实现智能的物品转移逻辑：</p>
     * <ul>
     *   <li>从存储区域点击：转移到玩家物品栏</li>
     *   <li>从玩家物品栏点击：转移到存储区域</li>
     *   <li>优先填充现有堆栈，再创建新堆栈</li>
     * </ul>
     * 
     * @param par1EntityPlayer 执行操作的玩家
     * @param i 被点击的槽位索引
     * @return 成功转移的物品堆栈副本，如果失败返回空堆栈
     */
    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(i);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int chestSlots = EnderItemStorage.sizes[chestInv.getSize()]; // 获取存储区域的槽位数量
            
            if (i < chestSlots) {
                // 从存储区域转移到玩家物品栏（从后往前填充）
                if (!moveItemStackTo(itemstack1, chestSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家物品栏转移到存储区域（从前往后填充）
                if (!moveItemStackTo(itemstack1, 0, chestSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            // 清理空槽位或标记槽位已改变
            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    /**
     * 容器关闭时的清理工作
     * 
     * <p>当玩家关闭GUI界面时调用，执行必要的清理工作，
     * 包括关闭存储实例和更新打开状态。</p>
     * 
     * @param entityplayer 关闭容器的玩家
     */
    @Override
    public void removed(Player entityplayer) {
        super.removed(entityplayer);
        chestInv.closeInventory(); // 标记存储为已关闭状态
    }
}
