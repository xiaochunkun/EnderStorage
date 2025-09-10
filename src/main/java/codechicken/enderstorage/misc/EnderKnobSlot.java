package codechicken.enderstorage.misc;

import codechicken.lib.vec.Vector3;
import net.minecraft.world.phys.AABB;

/**
 * 末影存储旋钮槽位类
 * 
 * 这个类用于计算末影存储设备（如末影箱和末影罐）上旋钮的
 * 碰撞箱和选择框。旋钮是玩家可以点击来切换频率颜色的3D元素。
 * 
 * 功能：
 * - 根据方块朝向计算旋钮的3D位置
 * - 生成准确的碰撞检测边界框
 * - 支持4个方向的旋转（北、东、南、西）
 * 
 * 旋钮的基础尺寸为0.125x0.25x0.0625个方块单位，
 * 然后根据方块的朝向进行相应的旋转变换。
 * 
 * @author EnderStorage团队
 */
public class EnderKnobSlot {

    /**
     * 构造函数
     * 
     * 根据方块的元数据（朝向）计算旋钮的精确位置和边界框。
     * 旋钮的基础形状是一个小的长方体，然后根据方块朝向进行旋转。
     * 
     * @param meta 方块元数据，表示方块的朝向（0-3分别对应北、东、南、西）
     */
    public EnderKnobSlot(int meta) {
        // 创建8个顶点来定义旋钮的长方体形状
        Vector3[] verts = new Vector3[8];
        // 定义旋钮的基础几何形状（面向北方时的位置）
        // 下面的4个顶点
        verts[0] = new Vector3(-0.0625, 0.4375, -0.5);   // 左下后
        verts[1] = new Vector3(0.0625, 0.4375, -0.5);    // 右下后
        verts[3] = new Vector3(-0.0625, 0.4375, -0.4375); // 左下前
        verts[2] = new Vector3(0.0625, 0.4375, -0.4375);  // 右下前
        // 上面的4个顶点
        verts[5] = new Vector3(-0.0625, 0.6875, -0.5);   // 左上后
        verts[4] = new Vector3(0.0625, 0.6875, -0.5);    // 右上后
        verts[6] = new Vector3(-0.0625, 0.6875, -0.4375); // 左上前
        verts[7] = new Vector3(0.0625, 0.6875, -0.4375);  // 右上前

        // 对所有顶点进行变换
        for (int i = 0; i < 8; i++) {
            // 根据方块朝向旋转顶点（围绕Y轴旋转）
            verts[i].rotate((meta + 2) * -0.5 * 3.14159, new Vector3(0, 1, 0));
            // 将顶点移动到方块中心（从-0.5~0.5范围移动到0~1范围）
            verts[i].add(0.5, 0, 0.5);
        }

        // 从顶点数组生成边界框
        aabb = cornersToAABB(verts);
    }

    /** 旋钮的边界框，用于碎撞检测和选择 */
    private final AABB aabb;

    /**
     * 获取旋钮的选择边界框
     * 
     * 这个边界框用于确定玩家是否点击了旋钮。
     * 
     * @return 旋钮的AABB边界框
     */
    public AABB getSelectionBB() {
        return aabb;
    }

    /**
     * 将顶点数组转换为AABB边界框
     * 
     * 遍历所有顶点，找到最小和最大的x、y、z坐标，
     * 然后创建包围所有顶点的最小边界框。
     * 
     * @param corners 顶点数组
     * @return 包围所有顶点的AABB边界框
     */
    public static AABB cornersToAABB(Vector3[] corners) {
        // 使用第一个顶点初始化最小和最大值
        Vector3 min = corners[0].copy();
        Vector3 max = corners[0].copy();
        // 遍历剩余顶点，更新最小和最大值
        for (int i = 1; i < corners.length; i++) {
            Vector3 vec = corners[i];
            // 更新X坐标范围
            if (vec.x < min.x) {
                min.x = vec.x;
            } else if (vec.x > max.x) {
                max.x = vec.x;
            }
            // 更新Y坐标范围
            if (vec.y < min.y) {
                min.y = vec.y;
            } else if (vec.y > max.y) {
                max.y = vec.y;
            }
            // 更新Z坐标范围
            if (vec.z < min.z) {
                min.z = vec.z;
            } else if (vec.z > max.z) {
                max.z = vec.z;
            }
        }
        // 创建并返回AABB边界框
        return new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
    }
}
