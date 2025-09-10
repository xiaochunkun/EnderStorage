package codechicken.enderstorage.storage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.lib.fluid.FluidUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * 末影液体存储类 - 提供跨维度的液体存储功能
 * 
 * <p>本类实现了末影存储系统中的液体存储功能，允许玩家在不同维度间共享液体资源。
 * 每个存储实例与特定的频率绑定，相同频率的液体存储在所有维度中同步。</p>
 * 
 * <h3>核心特性：</h3>
 * <ul>
 *   <li><strong>跨维度同步</strong>：相同频率的液体存储在所有维度中保持同步</li>
 *   <li><strong>大容量存储</strong>：默认容量为16桶（16000mB）</li>
 *   <li><strong>流体兼容性</strong>：支持所有NeoForge流体系统</li>
 *   <li><strong>实时同步</strong>：液体变化时立即标记为脏数据并同步</li>
 * </ul>
 * 
 * <h3>技术实现：</h3>
 * <ul>
 *   <li>继承自AbstractEnderStorage提供基础存储功能</li>
 *   <li>实现IFluidHandler接口提供标准流体操作</li>
 *   <li>实现IFluidTank接口提供容器访问</li>
 *   <li>使用内部Tank类处理液体变化事件</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>跨维度液体传输系统</li>
 *   <li>大容量液体缓冲区</li>
 *   <li>自动化生产线的液体存储</li>
 *   <li>多人服务器的共享液体仓库</li>
 * </ul>
 * 
 * @author ChickenBones
 * @since 1.0.0
 * @see AbstractEnderStorage
 * @see IFluidHandler
 * @see IFluidTank
 */
public class EnderLiquidStorage extends AbstractEnderStorage implements IFluidHandler, IFluidTank {

    /** 液体存储类型标识符 - 用于注册和识别液体存储类型 */
    public static final StorageType<EnderLiquidStorage> TYPE = new StorageType<>("liquid");

    /** 液体存储容量 - 16桶（16000mB），与原版末影箱体积相当 */
    public static final int CAPACITY = 16 * FluidUtils.B;

    /**
     * 内部液体储罐类 - 扩展FluidTank以支持变化监听
     * 
     * <p>当储罐内容发生变化时，自动触发setDirty()来标记存储需要同步，
     * 确保跨维度的液体状态保持一致。</p>
     */
    private class Tank extends FluidTank {

        /**
         * 构造函数
         * 
         * @param capacity 储罐容量（mB）
         */
        public Tank(int capacity) {
            super(capacity);
        }

        /**
         * 当储罐内容发生变化时的回调方法
         * 
         * <p>每当液体数量、类型发生变化时，都会调用此方法来标记存储为脏数据，
         * 触发后续的同步机制，确保其他维度中相同频率的液体存储保持同步。</p>
         */
        @Override
        protected void onContentsChanged() {
            setDirty();
        }
    }

    /** 内部储罐实例 - 实际存储液体的容器 */
    private Tank tank;

    /**
     * 构造函数 - 创建末影液体存储实例
     * 
     * @param manager 末影存储管理器，用于处理跨维度同步和数据持久化
     * @param freq 频率标识，决定哪些存储共享数据
     */
    public EnderLiquidStorage(EnderStorageManager manager, Frequency freq) {
        super(manager, freq);
        tank = new Tank(CAPACITY);
    }

    /**
     * 清空存储内容
     * 
     * <p>重置储罐为空状态，同时标记为脏数据以同步到其他维度。
     * 此操作通常在管理员命令或特殊情况下调用。</p>
     */
    @Override
    public void clearStorage() {
        tank = new Tank(CAPACITY);
        setDirty();
    }

    /**
     * 从NBT标签加载液体数据
     * 
     * <p>在服务器启动或维度加载时，从持久化存储中恢复液体状态。
     * 支持跨版本的数据兼容性。</p>
     * 
     * @param tag NBT标签，包含序列化的液体数据
     * @param registries 注册表提供者，用于解析液体类型
     */
    @Override
    public void loadFromTag(CompoundTag tag, HolderLookup.Provider registries) {
        tank.readFromNBT(registries, tag.getCompound("tank"));
    }

    /**
     * 获取存储类型标识
     * 
     * @return 返回"liquid"字符串，用于区分不同类型的末影存储
     */
    @Override
    public String type() {
        return "liquid";
    }

    /**
     * 将液体数据保存到NBT标签
     * 
     * <p>将当前液体状态序列化为NBT格式，用于持久化存储。
     * 确保服务器重启后能够正确恢复液体数据。</p>
     * 
     * @param registries 注册表提供者，用于序列化液体类型
     * @return 包含液体数据的NBT标签
     */
    @Override
    public CompoundTag saveToTag(HolderLookup.Provider registries) {
        CompoundTag compound = new CompoundTag();
        compound.put("tank", tank.writeToNBT(registries, new CompoundTag()));

        return compound;
    }

    // ==================== IFluidHandler 和 IFluidTank 接口委托方法 ====================
    // 以下方法直接委托给内部储罐，提供标准的流体操作接口
    
    //@formatter:off
    /** 获取储罐中的液体堆栈 */
    @Override public FluidStack getFluid() { return tank.getFluid(); }
    /** 获取当前液体数量 */
    @Override public int getFluidAmount() { return tank.getFluidAmount(); }
    /** 获取储罐总容量 */
    @Override public int getCapacity() { return tank.getCapacity(); }
    /** 检查液体是否有效 */
    @Override public boolean isFluidValid(FluidStack stack) { return tank.isFluidValid(stack); }
    /** 获取储罐数量（始终为1） */
    @Override public int getTanks() { return tank.getTanks(); }
    /** 获取指定储罐中的液体 */
    @Override public FluidStack getFluidInTank(int tankId) { return tank.getFluidInTank(tankId); }
    /** 获取指定储罐容量 */
    @Override public int getTankCapacity(int tankId) { return tank.getTankCapacity(tankId); }
    /** 检查指定储罐中的液体是否有效 */
    @Override public boolean isFluidValid(int tankId, FluidStack stack) { return tank.isFluidValid(tankId, stack); }
    /** 向储罐中填充液体 */
    @Override public int fill(FluidStack resource, FluidAction action) { return tank.fill(resource, action); }
    /** 从储罐中排出指定液体 */
    @Override public FluidStack drain(FluidStack resource, FluidAction action) { return tank.drain(resource, action); }
    /** 从储罐中排出指定数量的液体 */
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return tank.drain(maxDrain, action); }
    //@formatter:on
}
