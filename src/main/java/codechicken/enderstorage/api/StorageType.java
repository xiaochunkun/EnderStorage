package codechicken.enderstorage.api;

/**
 * 存储类型记录类
 * 
 * 这是一个简单的记录类，用于标识和区分不同类型的末影存储。
 * 每种存储类型都有一个唯一的名称标识符，用于存储管理器
 * 中的类型注册和查找机制。
 * 
 * 设计模式：
 * - 使用Java记录类提供不可变的数据结构
 * - 泛型约束确保类型安全
 * - 简单的标识符模式，便于扩展新的存储类型
 * 
 * 使用场景：
 * - 存储管理器中注册不同类型的存储插件
 * - 序列化和反序列化时的类型识别
 * - 运行时的类型检查和转换
 * 
 * @param <T> 继承自AbstractEnderStorage的存储类型
 * @param name 存储类型的名称标识符（如"item"、"liquid"等）
 * 
 * @author covers1624
 * @since 17/9/24
 */
public record StorageType<T extends AbstractEnderStorage>(String name) { }
