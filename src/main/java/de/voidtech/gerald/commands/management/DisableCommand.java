package main.java.de.voidtech.gerald.commands.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@Command
public class DisableCommand extends AbstractCommand {

	@Autowired
	private List<AbstractCommand> commands;

	@Autowired
	private List<AbstractRoutine> routines;

	@Autowired
	private ServerService serverService;

	@Override
	public void executeInternal(Message message, List<String> args) {

		if (!commands.contains(this)) commands.add(this);
		
		if (message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			String targetName = args.get(0).toLowerCase();
			if (targetName.equals("all")) disableAllCommands(message);
			else if (targetName.startsWith("r-")) disableRoutine(targetName, message);
			else disableCommand(targetName, message);
		}
	}

	private void disableCommand(String targetName, Message message) {
		AbstractCommand foundCommand = null;
		String resultMessage = "";
		for (AbstractCommand command : commands) {
			if (command.getName().equals(targetName) || Arrays.asList(command.getCommandAliases()).contains(targetName)) {
				foundCommand = command;
				break;
			}
		}
		if (foundCommand == null)
			resultMessage = "**No command was found with name `" + targetName + "`**";
		else if (!foundCommand.canBeDisabled()) 
			resultMessage = "**The command `"+ targetName + "` cannot be disabled/enabled!**";
		else {
			
			Server server = serverService.getServer(message.getGuild().getId());
			if (server.getCommandBlacklist().contains(targetName)) {
				resultMessage = "**This command is already disabled!**";
			} else {
				server.addToCommandBlacklist(targetName);
				serverService.saveServer(server);
				resultMessage = "**Command `" + targetName + "` has been disabled!**";
			}
		}
		message.getChannel().sendMessage(resultMessage).queue();
	}

	private void disableRoutine(String targetName, Message message) {
		AbstractRoutine foundRoutine = null;
		String resultMessage = "";
		for (AbstractRoutine routine: routines) {
			if (routine.getName().equals(targetName)) {
				foundRoutine = routine;
				break;
			}
		}
		
		if (foundRoutine == null) 
			resultMessage = "**No Routine was found with name `" + targetName + "`**";
		else if (!foundRoutine.canBeDisabled()) 
			resultMessage = "**Routine `"+ targetName + "` cannot be disabled/enabled!**";
		else {
			
			Server server = serverService.getServer(message.getGuild().getId());
			if (server.getRoutineBlacklist().contains(targetName))
				resultMessage = "**This routine is already disabled!**";
			else {
				server.addToRoutineBlacklist(targetName);
				serverService.saveServer(server);
				resultMessage = "**Routine `" + targetName + "`has been disabled!**";
			}
		}
		message.getChannel().sendMessage(resultMessage).queue();
	}

	private void disableAllCommands(Message message) {
		Server server = serverService.getServer(message.getGuild().getId());
		List<AbstractCommand> enabledCommands = new ArrayList<AbstractCommand>();
		for (AbstractCommand command : commands) {
			if (command.canBeDisabled() && !server.getCommandBlacklist().contains(command.getName()))
				server.addToCommandBlacklist(command.getName());
			else if (!command.canBeDisabled()) enabledCommands.add(command);		
		}
		serverService.saveServer(server);
		message.getChannel().sendMessage("**All commands have been disabled except for these:**\n```" + createEnabledCommandString(enabledCommands) + "```").queue();
	}

	private String createEnabledCommandString(List<AbstractCommand> enabledCommands) {
		String message = "";
		for (AbstractCommand command : enabledCommands)
			message += command.getName() + "\n";
		return message;
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
		String[] aliases = {"lock"};
		return aliases;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

}
