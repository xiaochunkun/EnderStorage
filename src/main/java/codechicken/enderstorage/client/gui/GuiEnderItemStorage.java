package codechicken.enderstorage.client.gui;

import codechicken.enderstorage.container.ContainerEnderItemStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * EnderStorage物品存储GUI界面
 * 
 * 提供EnderStorage物品存储容器的图形用户界面，主要功能包括：
 * - 容器界面渲染：显示存储容器和玩家背包界面
 * - 动态尺寸适应：根据存储容器大小调整界面高度
 * - 标签显示：显示容器标题、玩家背包标题和拥有者名称
 * - 材质选择：根据容器大小选择合适的背景材质
 * 
 * 界面特点：
 * - 支持不同大小的存储容器（小型、标准、大型）
 * - 自适应背景材质系统，重用原版GUI材质
 * - 显示频率拥有者信息，便于识别容器归属
 * - 标准的容器GUI交互模式
 * 
 * @author EnderStorage Team
 * @since 1.0.0
 */
public class GuiEnderItemStorage extends AbstractContainerScreen<ContainerEnderItemStorage> {

    /**
     * 构造函数
     * 
     * 初始化EnderStorage物品存储GUI界面。
     * 根据容器大小动态调整界面高度。
     * 
     * @param container 容器实例，包含存储数据和逻辑
     * @param playerInv 玩家背包实例
     * @param title 容器显示标题
     */
    public GuiEnderItemStorage(ContainerEnderItemStorage container, Inventory playerInv, Component title) {
        super(container, playerInv, title);

        // 对于大型容器（双箱模式），调整界面高度
        if (container.chestInv.getSize() == 2) {
            imageHeight = 222; // 双箱高度
        }
    }

    /**
     * 渲染整个GUI界面
     * 
     * 按顺序渲染背景、容器内容和工具提示。
     * 这是渲染流程的主入口方法。
     * 
     * @param graphics 图形渲染上下文
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTicks 部分tick时间，用于平滑动画
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks); // 渲染背景
        super.render(graphics, mouseX, mouseY, partialTicks);     // 渲染容器内容
        renderTooltip(graphics, mouseX, mouseY);                 // 渲染工具提示
    }

    /**
     * 渲染GUI标签文本
     * 
     * 显示容器标题、玩家背包标题和频率拥有者名称。
     * 所有文本都使用暗灰色（0x404040）以保持视觉一致性。
     * 
     * @param graphics 图形渲染上下文
     * @param mouseX 鼠标X坐标（未使用）
     * @param mouseY 鼠标Y坐标（未使用）
     */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 渲染容器标题（左上角）
        graphics.drawString(font, title.getVisualOrderText(), 8, 6, 0x404040, false);
        
        // 渲染玩家背包标题（容器下方）
        graphics.drawString(font, playerInventoryTitle.getVisualOrderText(), 8, imageHeight - 94, 0x404040, false);
        
        // 渲染频率拥有者名称（右上角，右对齐）
        menu.chestInv.freq.ownerName().ifPresent(name -> {
            graphics.drawString(font, name.getVisualOrderText(), 170 - font.width(name), 6, 0x404040, false);
        });
    }

    /**
     * 渲染背景材质
     * 
     * 根据容器大小选择合适的原版材质进行渲染：
     * - Size 0: 使用发射器材质（3x3格子）
     * - Size 1: 使用标准箱子材质，分段渲染以适应高度
     * - Size 2: 使用大型箱子材质（6x9格子）
     * 
     * 通过重用原版材质确保视觉一致性和资源效率。
     * 
     * @param graphics 图形渲染上下文
     * @param partialTicks 部分tick时间，用于平滑动画
     * @param mouseX 鼠标X坐标（未使用）
     * @param mouseY 鼠标Y坐标（未使用）
     */
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        // 根据容器大小选择材质
        ResourceLocation texture = ResourceLocation.withDefaultNamespace(
            menu.chestInv.getSize() == 0 ? 
            "textures/gui/container/dispenser.png" :     // 小型容器使用发射器材质
            "textures/gui/container/generic_54.png"      // 标准和大型容器使用通用材质
        );
        
        // 计算居中位置
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 根据容器大小执行不同的渲染策略
        switch (menu.chestInv.getSize()) {
            case 0: // 小型容器（3x3）
            case 2: // 大型容器（6x9）
                // 直接渲染完整材质
                graphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight);
                break;
            case 1: // 标准容器（3x9）
                // 分段渲染以适应标准箱子高度
                graphics.blit(texture, x, y, 0, 0, imageWidth, 71);       // 上半部分
                graphics.blit(texture, x, y + 71, 0, 126, imageWidth, 96); // 下半部分（背包区域）
                break;
        }
    }
}
