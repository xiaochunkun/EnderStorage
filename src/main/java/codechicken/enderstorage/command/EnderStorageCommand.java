/**
 * 末影存储主命令类（已废弃）
 * 
 * 这是旧版本的命令系统实现，目前已被注释掉。
 * 在新版本的Minecraft中，命令系统已经重构，需要使用新的命令API。
 * 
 * 原功能包括：
 * - 提供帮助命令显示使用说明
 * - 集成清理命令用于管理末影存储
 * - 支持多种命令别名（ES、es、EnderStorage、enderstorage）
 * - 提供颜色、频率、有效存储等帮助页面
 * 
 * TODO: 需要使用新的命令系统重新实现这些功能
 * 
 * @author covers1624
 * @since 18/01/2017
 * @deprecated 使用旧版命令API，需要重构
 */
//package codechicken.enderstorage.command;
//
//import codechicken.enderstorage.command.help.ColourHelp;
//import codechicken.enderstorage.command.help.FrequencyHelp;
//import codechicken.enderstorage.command.help.ValidStorageHelp;
//import codechicken.lib.command.help.HelpCommandBase;
//import codechicken.lib.command.help.IHelpCommandHost;
//import com.google.common.collect.ImmutableList;
//import net.minecraft.command.CommandException;
//import net.minecraft.command.ICommand;
//import net.minecraft.command.ICommandSender;
//import net.minecraft.server.MinecraftServer;
//import net.minecraftforge.server.command.CommandTreeBase;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by covers1624 on 18/01/2017.
// */
//public class EnderStorageCommand extends CommandTreeBase implements IHelpCommandHost {
//
//    private HelpCommandBase helpCommand;
//
//    public EnderStorageCommand() {
//        helpCommand = new HelpCommandBase(this);
//        addSubcommand(helpCommand);
//        addSubcommand(new ClearCommand());
//        helpCommand.addHelpPage(new ColourHelp());
//        helpCommand.addHelpPage(new FrequencyHelp());
//        helpCommand.addHelpPage(new ValidStorageHelp());
//    }
//
//    @Override
//    public String getName() {
//        return "EnderStorage";
//    }
//
//    @Override
//    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//        if (args.length < 1) {
//            helpCommand.displayHelp(server, sender);
//        } else {
//            super.execute(server, sender, args);
//        }
//    }
//
//    @Override
//    public List<String> getAliases() {
//        //TODO Is "ES" already used by someone?
//        return ImmutableList.of("ES", "es", "EnderStorage", "enderstorage");
//    }
//
//    @Override
//    public String getUsage(ICommandSender sender) {
//        return "/" + getName() + " help";
//    }
//
//    @Override
//    public int getRequiredPermissionLevel() {
//        return 0;
//    }
//
//    @Override
//    public Map<String, ICommand> getSubCommandMap() {
//        return getCommandMap();
//    }
//
//    @Override
//    public String getParentName() {
//        return getName();
//    }
//}
