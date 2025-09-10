/**
 * 颜色帮助页面类（已废弃）
 * 
 * 这是旧版本命令系统的颜色帮助页面实现，目前已被注释掉。
 * 用于显示末影存储中可用的所有颜色名称。
 * 
 * 原功能：
 * - 列出所有可用的颜色名称
 * - 为用户提供颜色参数的参考
 * - 与主命令系统集成
 * 
 * TODO: 需要在新的命令系统中重新实现颜色帮助功能
 * 
 * @author covers1624  
 * @since 23/01/2017
 * @deprecated 使用旧版命令API，需要重构
 */
//package codechicken.enderstorage.command.help;
//
//import codechicken.lib.colour.EnumColour;
//import codechicken.lib.command.help.IHelpPage;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by covers1624 on 23/01/2017.
// */
//public class ColourHelp implements IHelpPage {
//
//    @Override
//    public String getName() {
//        return "colour";
//    }
//
//    @Override
//    public String getDesc() {
//        return "Displays the valid colours used by EnderStorage";
//    }
//
//    @Override
//    public List<String> getHelp() {
//        List<String> list = new ArrayList<>();
//        list.add("A colour can be one of the following names: ");
//        for (EnumColour colour : EnumColour.values()) {
//            list.add(colour.getName());
//        }
//        return list;
//    }
//}
