package main.java.de.voidtech.gerald.commands.management;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class EnableCommand extends AbstractCommand {

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
			if (targetName.equals("all")) enableAllCommands(context);
			else if (targetName.startsWith("r-")) enableRoutine(targetName, context);
			else enableCommand(targetName, context);
		}
	}
	
	private void enableCommand(String targetName, CommandContext context) {
		AbstractCommand foundCommand = null;
		String resultMessage = "";
		for (AbstractCommand command : commands) {
			if (command.getName().equals(targetName)) {
				foundCommand = command;
				targetName = command.getName();
				break;
			}
		}
		if (foundCommand == null)
			resultMessage = "**No command was found with name `" + targetName + "`**";
		else if (!foundCommand.canBeDisabled()) context.reply("**The command `"+ targetName + "` cannot be enabled/disabled!**");
		else {
			Server server = serverService.getServer(context.getGuild().getId());
			if (!server.getCommandBlacklist().contains(targetName)) {
				resultMessage = "**This command is not disabled!**";
			} else {
				server.removeFromCommandBlacklist(targetName);
				serverService.saveServer(server);
				resultMessage = "**Command `" + targetName + "` has been enabled!**";
			}
		}
		context.reply(resultMessage);
	}

	private void enableRoutine(String targetName, CommandContext context) {
		AbstractRoutine foundRoutine = null;
		String resultMessage;
		for (AbstractRoutine routine: routines) {
			if (routine.getName().equals(targetName)) {
				foundRoutine = routine;
				break;
			}
		}
		if (foundRoutine == null) 
			resultMessage = "**No Routine was found with name `" + targetName + "`**";
		else if (!foundRoutine.canBeDisabled()) 
			resultMessage = "**Routine `"+ targetName + "` cannot be enabled/disabled!**";
		else {
			
			Server server = serverService.getServer(context.getGuild().getId());
			if (!server.getRoutineBlacklist().contains(targetName))
				resultMessage = "**Routine `" + foundRoutine.getName() + "` is not disabled!**";
			else {
				server.removeFromRoutineBlacklist(targetName);
				serverService.saveServer(server);
				resultMessage = "**Routine `" + targetName + "`has been enabled!**";
			}
		}
		context.reply(resultMessage);
	}

	private void enableAllCommands(CommandContext context) {
		Server server = serverService.getServer(context.getGuild().getId());
		String resultMessage;
		if (server.getCommandBlacklist().isEmpty())
			resultMessage = "**There are no disabled commands to enable!**";
		else {
			server.clearCommandBlacklist();
			serverService.saveServer(server);
			resultMessage = "**All commands have been enabled!**";	
		}
		context.reply(resultMessage);
	}

	@Override
	public String getDescription() {
		return "Allows you to enable a command or routine! Note: Some routines or commands cannot be disabled. Routine names always use the format r-[name]";
	}

	@Override
	public String getUsage() {
		return "enable r-nitrolite\n"
				+ "enable ping";
	}

	@Override
	public String getName() {
		return "enable";
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
		return null;
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