package codechicken.enderstorage.network;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderLiquidStorage;
import codechicken.lib.math.MathHelper;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.ClientUtils;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * 液体罐同步器
 * 
 * 负责管理EnderStorage液体罐在客户端和服务器之间的状态同步，主要功能包括：
 * - 液体状态同步：确保客户端显示的液体内容与服务器一致
 * - 可见性管理：根据客户端需求优化网络传输
 * - 动画平滑：提供液体变化的平滑过渡效果
 * - 玩家状态跟踪：管理每个玩家的液体罐可见性状态
 * 
 * 核心设计特点：
 * - 差量同步：只在状态发生显著变化时同步，减少网络负载
 * - 插值动画：客户端使用插值实现液体变化的平滑动画
 * - 按需同步：只同步玩家当前可见的液体罐状态
 * - 事件驱动：响应玩家登录、切换维度等事件管理同步状态
 * 
 * @author EnderStorage Team
 * @since 1.0.0
 */
public class TankSynchroniser {

    /**
     * 液体罐状态抽象基类
     * 
     * 定义液体罐状态的基本数据结构和同步逻辑，包括：
     * - 频率标识：用于区分不同的液体罐
     * - 多种液体状态：客户端、服务器、过渡状态
     * - 同步更新逻辑：处理状态变化和动画插值
     * 
     * 状态说明：
     * - c_liquid: 客户端当前显示的液体状态（用于渲染）
     * - s_liquid: 服务器的实际液体状态（权威数据）
     * - f_liquid: 过渡动画的起始状态（用于平滑动画）
     */
    public static abstract class TankState {

        /** 液体罐的频率标识，用于区分不同的液体罐实例 */
        public Frequency frequency = new Frequency();
        
        /** 客户端当前液体状态，用于渲染显示 */
        public FluidStack c_liquid = new FluidStack(Fluids.WATER, 0);
        
        /** 服务器液体状态，权威数据源 */
        public FluidStack s_liquid = new FluidStack(Fluids.WATER, 0);
        
        /** 过渡动画起始状态，用于平滑液体变化 */
        public FluidStack f_liquid = new FluidStack(Fluids.WATER, 0);

        /**
         * 设置液体罐的频率标识
         * 
         * @param frequency 新的频率标识
         */
        public void setFrequency(Frequency frequency) {
            this.frequency = frequency;
        }

        /**
         * 更新液体罐状态
         * 
         * 核心同步逻辑，根据客户端/服务器模式执行不同的更新策略：
         * 
         * 客户端模式：
         * - 使用指数插值实现液体数量的平滑过渡
         * - 处理液体类型变化时的过渡动画
         * - 确保渲染的连续性和流畅性
         * 
         * 服务器模式：
         * - 从存储系统获取权威液体状态
         * - 检测显著变化并触发网络同步
         * - 优化网络传输，避免频繁的小幅度更新
         * 
         * @param client true表示客户端模式，false表示服务器模式
         */
        public void update(boolean client) {
            FluidStack b_liquid; // 更新前的液体状态
            FluidStack a_liquid; // 更新后的液体状态
            
            if (client) {
                // === 客户端更新逻辑 ===
                b_liquid = c_liquid.copy();

                // 处理相同液体类型或空液体的平滑过渡
                if (FluidStack.isSameFluidSameComponents(s_liquid, c_liquid) || c_liquid.isEmpty()) {
                    // 使用指数逼近算法实现平滑的数量变化
                    int change = MathHelper.approachExpI(c_liquid.getAmount(), s_liquid.getAmount(), 0.1);
                    if (c_liquid.isEmpty()) {
                        c_liquid = s_liquid.copyWithAmount(change);
                    } else {
                        c_liquid.setAmount(change);
                    }
                } else if (c_liquid.getAmount() > 100) {
                    // 处理液体类型变化：逐渐减少当前液体直至消失
                    c_liquid.setAmount(MathHelper.retreatExpI(c_liquid.getAmount(), 0, f_liquid.getAmount(), 0.1, 1000));
                }

                a_liquid = c_liquid;
            } else {
                // === 服务器更新逻辑 ===
                s_liquid = getStorage(false).getFluid(); // 获取存储系统的实际液体状态
                b_liquid = s_liquid.copy();
                
                // 检测液体类型变化
                if (!FluidStack.isSameFluidSameComponents(s_liquid, c_liquid)) {
                    sendSyncPacket();
                    c_liquid = s_liquid.copy();
                } else if (Math.abs(c_liquid.getAmount() - s_liquid.getAmount()) > 250 || 
                          (s_liquid.getAmount() == 0 && c_liquid.getAmount() > 0)) {
                    // 检测显著的数量变化（差值>250）或服务器清空但客户端仍有液体
                    sendSyncPacket();
                    c_liquid = s_liquid.copy();
                }

                a_liquid = s_liquid;
            }
            
            // 检测液体状态是否发生了实质性变化（空/非空状态或液体类型）
            if ((b_liquid.getAmount() == 0) != (a_liquid.getAmount() == 0) || 
                !FluidStack.isSameFluidSameComponents(b_liquid, a_liquid)) {
                onLiquidChanged(); // 触发液体变化事件
            }
        }

        /**
         * 液体状态变化事件回调
         * 
         * 当液体罐的液体状态发生实质性变化时触发，
         * 子类可以重写此方法来处理特定的变化逻辑。
         */
        public void onLiquidChanged() {
        }

        /**
         * 发送同步数据包（抽象方法）
         * 
         * 子类必须实现此方法来定义具体的网络同步逻辑。
         * 根据不同的实现类型，可能是全局同步或特定玩家同步。
         */
        public abstract void sendSyncPacket();

        /**
         * 同步液体状态（客户端接收）
         * 
         * 当接收到服务器的同步数据包时调用，更新服务器状态
         * 并为不同液体类型的变化准备过渡动画。
         * 
         * @param liquid 服务器端的最新液体状态
         */
        public void sync(FluidStack liquid) {
            s_liquid = liquid;
            // 如果液体类型发生变化，保存当前状态作为过渡动画的起始点
            if (!FluidStack.isSameFluidSameComponents(s_liquid, c_liquid)) {
                f_liquid = c_liquid.copy();
            }
        }

        /**
         * 获取液体存储实例（仅服务器端）
         * 
         * 通过频率标识从存储管理器中获取对应的液体存储实例。
         * 此方法只在服务器端使用，用于获取权威的液体数据。
         * 
         * @param client 是否为客户端模式（对于此方法应为false）
         * @return 对应频率的液体存储实例
         */
        //SERVER SIDE ONLY!
        public EnderLiquidStorage getStorage(boolean client) {
            return EnderStorageManager.instance(client).getStorage(frequency, EnderLiquidStorage.TYPE);
        }
    }

    /**
     * 玩家物品液体罐状态类
     * 
     * 专门用于管理玩家持有的Ender液体罐物品的状态同步，特点包括：
     * - 玩家关联：与特定玩家绑定，管理该玩家的液体罐状态
     * - 跟踪控制：可以动态开启或关闭状态跟踪，优化网络性能
     * - 按需同步：只在玩家需要时进行状态同步和更新
     * 
     * 使用场景：
     * - 玩家在背包中查看Ender液体罐物品时
     * - 玩家手持Ender液体罐物品时
     * - 玩家在GUI界面中操作液体罐时
     */
    public static class PlayerItemTankState extends TankState {

        /** 关联的服务器玩家实例，用于发送同步数据包 */
        private @Nullable ServerPlayer player;
        
        /** 跟踪状态标志，true表示正在跟踪并同步此液体罐状态 */
        private boolean tracking;

        /**
         * 构造函数（服务器端使用）
         * 
         * 创建与特定玩家和液体存储关联的状态实例。
         * 初始化时默认开启跟踪模式。
         * 
         * @param player 关联的服务器玩家
         * @param storage 液体存储实例，用于获取频率信息
         */
        public PlayerItemTankState(ServerPlayer player, EnderLiquidStorage storage) {
            this.player = player;
            setFrequency(storage.freq);
            tracking = true; // 默认开启跟踪
        }

        /**
         * 默认构造函数（客户端使用）
         * 
         * 创建不与特定玩家关联的状态实例，
         * 主要用于客户端接收和处理同步数据。
         */
        public PlayerItemTankState() {
        }

        /**
         * 发送同步数据包到关联的玩家
         * 
         * 当需要同步液体状态时，向关联的玩家发送网络数据包。
         * 只有在跟踪模式开启时才会实际发送数据包。
         */
        @Override
        public void sendSyncPacket() {
            if (!tracking) {
                return; // 未开启跟踪时不发送数据包
            }

            assert player != null;
            // 创建液体罐同步数据包
            PacketCustom packet = new PacketCustom(EnderStorageNetwork.NET_CHANNEL, EnderStorageNetwork.C_TANK_SYNC, player.registryAccess());
            getStorage(false).freq.writeToPacket(packet); // 写入频率信息
            packet.writeFluidStack(s_liquid);              // 写入液体状态
            packet.sendToPlayer(player);                   // 发送给目标玩家
        }

        /**
         * 设置跟踪状态
         * 
         * 控制是否跟踪并同步此液体罐的状态。
         * 用于优化性能，只在需要时进行状态同步。
         * 
         * @param t true表示开启跟踪，false表示关闭跟踪
         */
        public void setTracking(boolean t) {
            tracking = t;
        }

        /**
         * 更新液体罐状态（重写）
         * 
         * 在基类的更新逻辑基础上添加跟踪状态检查。
         * 只有在跟踪模式开启或客户端模式下才执行更新。
         * 
         * @param client 是否为客户端模式
         */
        @Override
        public void update(boolean client) {
            if (tracking || client) {
                super.update(client);
            }
        }
    }

    /**
     * 玩家物品液体罐缓存管理器
     * 
     * 管理单个玩家的所有液体罐状态缓存，提供以下核心功能：
     * - 状态缓存：管理多个频率的液体罐状态实例
     * - 可见性跟踪：跟踪客户端当前可见的液体罐频率
     * - 网络优化：实现增量同步，只传输变化的可见性信息
     * - 双端支持：同时支持客户端和服务器端的不同逻辑
     * 
     * 工作原理：
     * - 服务器端：为每个玩家维护一个缓存实例，管理该玩家的同步状态
     * - 客户端：维护全局缓存实例，处理来自服务器的同步数据
     * - 可见性管理：使用双缓冲机制检测可见性变化，优化网络传输
     */
    public static class PlayerItemTankCache {

        /** 是否为客户端模式，决定不同的处理逻辑 */
        private final boolean client;
        
        /** 液体罐状态映射表，以频率字符串为键，状态实例为值 */
        private final Map<String, PlayerItemTankState> tankStates = new HashMap<>();
        
        // === 客户端可见性管理字段 ===
        
        /** 当前帧的可见频率集合 */
        private HashSet<Frequency> a_visible = new HashSet<>();
        
        /** 上一帧的可见频率集合，用于对比变化 */
        private HashSet<Frequency> b_visible = new HashSet<>();
        
        // === 服务器端字段 ===
        
        /** 关联的服务器玩家实例（仅服务器端） */
        private @Nullable ServerPlayer player;

        /**
         * 构造函数（服务器端使用）
         * 
         * 创建与特定玩家关联的缓存管理器。
         * 
         * @param player 关联的服务器玩家
         */
        public PlayerItemTankCache(ServerPlayer player) {
            this.player = player;
            client = false;
        }

        /**
         * 默认构造函数（客户端使用）
         * 
         * 创建客户端全局缓存管理器实例。
         */
        public PlayerItemTankCache() {
            client = true;
        }

        /**
         * 跟踪指定频率的液体罐状态
         * 
         * 控制是否对特定频率的液体罐进行状态跟踪和同步。
         * 在服务器端用于优化网络传输，只同步玩家需要的液体罐。
         * 
         * @param freq 要跟踪的液体罐频率
         * @param t true表示开启跟踪，false表示停止跟踪
         */
        public void track(Frequency freq, boolean t) {
            String key = freq.toString();
            PlayerItemTankState state = tankStates.get(key);
            if (state == null) {
                if (!t) {
                    return; // 不需要跟踪且状态不存在，直接返回
                }
                assert player != null;
                // 创建新的状态实例
                tankStates.put(key, state = new PlayerItemTankState(player, EnderStorageManager.instance(false).getStorage(freq, EnderLiquidStorage.TYPE)));
            }
            state.setTracking(t);
        }

        /**
         * 同步液体状态（客户端接收）
         * 
         * 当客户端接收到服务器的液体同步数据包时调用。
         * 自动创建或更新对应频率的状态实例。
         * 
         * @param freq 液体罐频率
         * @param liquid 新的液体状态
         */
        public void sync(Frequency freq, FluidStack liquid) {
            String key = freq.toString();
            // 如果状态不存在则自动创建
            PlayerItemTankState state = tankStates.computeIfAbsent(key, k -> new PlayerItemTankState());
            state.sync(liquid);
        }

        public void update() {
            for (Map.Entry<String, PlayerItemTankState> entry : tankStates.entrySet()) {
                entry.getValue().update(client);
            }

            if (client) {
                Sets.SetView<Frequency> new_visible = Sets.difference(a_visible, b_visible);
                Sets.SetView<Frequency> old_visible = Sets.difference(b_visible, a_visible);

                if (!new_visible.isEmpty() || !old_visible.isEmpty()) {
                    PacketCustom packet = new PacketCustom(EnderStorageNetwork.NET_CHANNEL, EnderStorageNetwork.S_VISIBILITY, Minecraft.getInstance().player.registryAccess());

                    packet.writeShort(new_visible.size());
                    new_visible.forEach(freq -> freq.writeToPacket(packet));

                    packet.writeShort(old_visible.size());
                    old_visible.forEach(freq -> freq.writeToPacket(packet));

                    packet.sendToServer();
                }

                HashSet<Frequency> temp = b_visible;
                temp.clear();
                b_visible = a_visible;
                a_visible = temp;
            }
        }

        public FluidStack getLiquid(Frequency freq) {
            String key = freq.toString();
            a_visible.add(freq);
            PlayerItemTankState state = tankStates.get(key);
            return state == null ? FluidStack.EMPTY : state.c_liquid;
        }

        public void handleVisiblityPacket(PacketCustom packet) {
            int k = packet.readUShort();
            for (int i = 0; i < k; i++) {
                track(Frequency.readFromPacket(packet), true);
            }
            k = packet.readUShort();
            for (int i = 0; i < k; i++) {
                track(Frequency.readFromPacket(packet), false);
            }
        }
    }

    private static Map<UUID, PlayerItemTankCache> playerItemTankStates = new HashMap<>();
    private static @Nullable PlayerItemTankCache clientState;

    public static void syncClient(Frequency freq, FluidStack liquid) {
        if (clientState != null) {
            clientState.sync(freq, liquid);
        }
    }

    public static FluidStack getClientLiquid(Frequency freq) {
        if (clientState != null) {
            return clientState.getLiquid(freq);
        }
        return FluidStack.EMPTY;
    }

    public static void handleVisiblityPacket(ServerPlayer player, PacketCustom packet) {
        var cache = playerItemTankStates.get(player.getUUID());
        if (cache != null) {
            cache.handleVisiblityPacket(packet);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        playerItemTankStates.put(event.getEntity().getUUID(), new PlayerItemTankCache((ServerPlayer) event.getEntity()));
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        playerItemTankStates.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        playerItemTankStates.put(event.getEntity().getUUID(), new PlayerItemTankCache((ServerPlayer) event.getEntity()));
    }

    @SubscribeEvent
    public void tickEnd(ServerTickEvent.Post event) {
        for (Map.Entry<UUID, PlayerItemTankCache> entry : playerItemTankStates.entrySet()) {
            entry.getValue().update();
        }
    }

    @SubscribeEvent
    public void tickEnd(ClientTickEvent.Post event) {
        if (ClientUtils.inWorld() && clientState != null) {
            clientState.update();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && !serverLevel.getServer().isRunning()) {
            playerItemTankStates.clear();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            clientState = new PlayerItemTankCache();
        }
    }
}
