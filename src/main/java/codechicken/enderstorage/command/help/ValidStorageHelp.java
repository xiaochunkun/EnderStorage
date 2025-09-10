/**
 * 有效存储帮助页面类（已废弃）
 * 
 * 这是旧版本命令系统的有效存储帮助页面实现，目前已被注释掉。
 * 用于显示末影存储管理的所有有效存储类型标识符。
 * 
 * 原功能：
 * - 显示所有已安装的末影存储插件标识符
 * - 解释通配符"*"的含义（表示所有插件）
 * - 动态列出当前可用的存储系统
 * - 为命令参数提供参考信息
 * 
 * TODO: 需要在新的命令系统中重新实现有效存储帮助功能
 * 
 * @author covers1624
 * @since 18/01/2017
 * @deprecated 使用旧版命令API，需要重构
 */
//package codechicken.enderstorage.command.help;
//
//import codechicken.enderstorage.api.EnderStoragePlugin;
//import codechicken.enderstorage.manager.EnderStorageManager;
//import codechicken.lib.command.help.IHelpPage;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map.Entry;
//
///**
// * Created by covers1624 on 18/01/2017.
// */
//public class ValidStorageHelp implements IHelpPage {
//
//    @Override
//    public String getName() {
//        return "validStorage";
//    }
//
//    @Override
//    public String getDesc() {
//        return "Displays the valid key words for systems managed by EnderStorage.";
//    }
//
//    @Override
//    public List<String> getHelp() {
//        List<String> list = new ArrayList<>();
//        list.add("This directly references what plugins are installed to EnderStorage");
//        list.add("\"*\" is a valid keyword and essentially means All Plugins.");
//        list.add("Valid keywords:");
//        for (Entry<String, EnderStoragePlugin> entry : EnderStorageManager.getPlugins().entrySet()) {
//            list.add(" " + entry.getKey());
//        }
//        return list;
//    }
//}
