/**
 * 频率帮助页面类（已废弃）
 * 
 * 这是旧版本命令系统的频率帮助页面实现，目前已被注释掉。
 * 用于说明在末影存储命令中如何格式化频率参数。
 * 
 * 原功能：
 * - 解释频率参数的格式规则
 * - 说明颜色名称的使用方法
 * - 提供频率格式示例："<colour>,<colour>,<colour>"
 * - 支持大小写不敏感的颜色名称
 * 
 * TODO: 需要在新的命令系统中重新实现频率帮助功能
 * 
 * @author covers1624
 * @since 18/01/2017
 * @deprecated 使用旧版命令API，需要重构
 */
//package codechicken.enderstorage.command.help;
//
//import codechicken.lib.command.help.IHelpPage;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by covers1624 on 18/01/2017.
// */
//public class FrequencyHelp implements IHelpPage {
//
//    @Override
//    public String getName() {
//        return "frequency";
//    }
//
//    @Override
//    public String getDesc() {
//        return "Shows you how frequency is formatted inside EnderStorage commands.";
//    }
//
//    @Override
//    public List<String> getHelp() {
//        List<String> list = new ArrayList<>();
//        list.add("Frequency for commands is defined as follows:");
//        list.add("\"<colour>,<colour>,<colour>\"");
//        list.add("Colour must be the name of the colour i.e. \"red\"");
//        list.add("Colour can be Upper or Lowercase.");
//        return list;
//    }
//}
