package main.java.de.voidtech.gerald.commands.management;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Command
public class DisableCommand extends AbstractCommand {

    @Autowired
    private List<AbstractCommand> commands;

    @Autowired
    private List<AbstractRoutine> routines;

    @Autowired
    private ServerService serverService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {

        if (!commands.contains(this)) commands.add(this);

        if (context.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            String targetName = args.get(0).toLowerCase();
            if (targetName.equals("all")) disableAllCommands(context);
            else if (targetName.startsWith("r-")) disableRoutine(targetName, context);
            else disableCommand(targetName, context);
        }
    }

    private void disableCommand(String targetName, CommandContext context) {
        AbstractCommand foundCommand = null;
        String resultMessage;
        for (AbstractCommand command : commands) {
            if (command.getName().equals(targetName)) {
                foundCommand = command;
                break;
            }
        }
        if (foundCommand == null)
            resultMessage = "**No command was found with name `" + targetName + "`**";
        else if (!foundCommand.canBeDisabled())
            resultMessage = "**The command `" + targetName + "` cannot be disabled/enabled!**";
        else {

            Server server = serverService.getServer(context.getGuild().getId());
            if (server.getCommandBlacklist().contains(targetName)) {
                resultMessage = "**This command is already disabled!**";
            } else {
                server.addToCommandBlacklist(targetName);
                serverService.saveServer(server);
                resultMessage = "**Command `" + targetName + "` has been disabled!**";
            }
        }
        context.reply(resultMessage);
    }

    private void disableRoutine(String targetName, CommandContext context) {
        AbstractRoutine foundRoutine = null;
        String resultMessage;
        for (AbstractRoutine routine : routines) {
            if (routine.getName().equals(targetName)) {
                foundRoutine = routine;
                break;
            }
        }

        if (foundRoutine == null)
            resultMessage = "**No Routine was found with name `" + targetName + "`**";
        else if (!foundRoutine.canBeDisabled())
            resultMessage = "**Routine `" + targetName + "` cannot be disabled/enabled!**";
        else {

            Server server = serverService.getServer(context.getGuild().getId());
            if (server.getRoutineBlacklist().contains(targetName))
                resultMessage = "**Routine `" + foundRoutine.getName() + "` is already disabled!**";
            else {
                server.addToRoutineBlacklist(targetName);
                serverService.saveServer(server);
                resultMessage = "**Routine `" + targetName + "` has been disabled!**";
            }
        }
        context.reply(resultMessage);
    }

    private void disableAllCommands(CommandContext context) {
        Server server = serverService.getServer(context.getGuild().getId());
        List<AbstractCommand> enabledCommands = new ArrayList<AbstractCommand>();
        for (AbstractCommand command : commands) {
            if (command.canBeDisabled() && !server.getCommandBlacklist().contains(command.getName()))
                server.addToCommandBlacklist(command.getName());
            else if (!command.canBeDisabled()) enabledCommands.add(command);
        }
        serverService.saveServer(server);
        context.reply("**All commands have been disabled except for these:**\n```" + createEnabledCommandString(enabledCommands) + "```");
    }

    private String createEnabledCommandString(List<AbstractCommand> enabledCommands) {
        StringBuilder message = new StringBuilder();
        for (AbstractCommand command : enabledCommands)
            message.append(command.getName()).append("\n");
        return message.toString();
    }

    @Override
    public String getDescription() {
        return "Allows you to disable a command or routine! Note: Some routines or commands cannot be disabled. Routine names always use the format r-[name]";
    }

    @Override
    public String getUsage() {
        return "disable r-nitrolite\n"
                + "disable ping";
    }

    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MANAGEMENT;
    }

    @Override
    public boolean isDMCapable() {
        return false;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[0];
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean isSlashCompatible() {
        return true;
    }

}