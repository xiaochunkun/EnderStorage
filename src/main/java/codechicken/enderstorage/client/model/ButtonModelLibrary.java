package codechicken.enderstorage.client.model;

import codechicken.enderstorage.tile.TileFrequencyOwner;
import codechicken.lib.render.CCModel;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.Vertex5;

/**
 * 按钮模型库
 * <p>
 * 负责创建和管理末影存储设备上颜色选择按钮的3D模型。
 * 这些按钮用于设置末影箱和末影储罐的频率颜色，每个设备有三个按钮
 * （左、中、右），每个按钮可以显示16种不同的颜色。
 * <p>
 * 该类使用静态初始化块创建按钮的几何模型，定义了按钮的顶点、
 * 纹理坐标和面法线，确保按钮在游戏中能够正确渲染。
 * <p>
 * 模型基于立方体几何结构，通过精确的顶点坐标和纹理映射
 * 来创建一个符合游戏美术风格的3D按钮。
 */
public class ButtonModelLibrary {

    /** 按钮的3D模型对象，包含所有几何数据和渲染信息 */
    public static CCModel button;

    // 静态初始化块：创建按钮的3D几何模型
    static {
        // 创建一个包含20个顶点的四边形模型（5个面 × 4个顶点）
        button = CCModel.quadModel(20);
        
        // 从频率拥有者获取按钮的边界框坐标
        Vector3 min = TileFrequencyOwner.SELECTION_BUTTON.min;
        Vector3 max = TileFrequencyOwner.SELECTION_BUTTON.max;
        
        // 定义立方体的8个角点坐标
        Vector3[] corners = new Vector3[8];
        corners[0] = new Vector3(min.x, min.y, min.z); // 前下左
        corners[1] = new Vector3(max.x, min.y, min.z); // 前下右
        corners[3] = new Vector3(min.x, max.y, min.z); // 前上左
        corners[2] = new Vector3(max.x, max.y, min.z); // 前上右
        corners[4] = new Vector3(min.x, min.y, max.z); // 后下左
        corners[5] = new Vector3(max.x, min.y, max.z); // 后下右
        corners[7] = new Vector3(min.x, max.y, max.z); // 后上左
        corners[6] = new Vector3(max.x, max.y, max.z); // 后上右

        int i = 0;
        Vertex5[] verts = button.verts;

        // 定义按钮的顶面（Y轴正方向）
        // 每个顶点包含3D坐标和2D纹理坐标
        verts[i++] = new Vertex5(corners[7], 0.0938, 0.0625); // 后上左
        verts[i++] = new Vertex5(corners[6], 0.1562, 0.0625); // 后上右
        verts[i++] = new Vertex5(corners[2], 0.1562, 0.1875); // 前上右
        verts[i++] = new Vertex5(corners[3], 0.0938, 0.1875); // 前上左

        // 定义按钮的底面（Y轴负方向）
        verts[i++] = new Vertex5(corners[4], 0.0938, 0.0313); // 后下左
        verts[i++] = new Vertex5(corners[5], 0.1562, 0.0624); // 后下右
        verts[i++] = new Vertex5(corners[6], 0.1562, 0.0624); // 后上右
        verts[i++] = new Vertex5(corners[7], 0.0938, 0.0313); // 后上左

        // 定义按钮的前面（Z轴负方向）
        verts[i++] = new Vertex5(corners[0], 0.0938, 0.2186); // 前下左
        verts[i++] = new Vertex5(corners[3], 0.0938, 0.1876); // 前上左
        verts[i++] = new Vertex5(corners[2], 0.1562, 0.1876); // 前上右
        verts[i++] = new Vertex5(corners[1], 0.1562, 0.2186); // 前下右

        // 定义按钮的右侧面（X轴正方向）
        verts[i++] = new Vertex5(corners[6], 0.1563, 0.0626); // 后上右
        verts[i++] = new Vertex5(corners[5], 0.1874, 0.0626); // 后下右
        verts[i++] = new Vertex5(corners[1], 0.1874, 0.1874); // 前下右
        verts[i++] = new Vertex5(corners[2], 0.1563, 0.1874); // 前上右

        // 定义按钮的左侧面（X轴负方向）
        verts[i++] = new Vertex5(corners[7], 0.0937, 0.0626); // 后上左
        verts[i++] = new Vertex5(corners[3], 0.0937, 0.1874); // 前上左
        verts[i++] = new Vertex5(corners[0], 0.0626, 0.1874); // 前下左
        verts[i++] = new Vertex5(corners[4], 0.0626, 0.0626); // 后下左

        // 计算所有面的法线向量，用于光照计算
        button.computeNormals();
    }
}
