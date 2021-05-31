package main.java.de.voidtech.gerald.commands.management;

import java.util.Arrays;
import java.util.List;

import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@Command
public class EnableCommand extends AbstractCommand {

	@Autowired
	private List<AbstractCommand> commands;
	
	@Autowired
	private List<AbstractRoutine> routines;

	@Autowired
	private ServerService serverService;

	@Override
	public void executeInternal(Message message, List<String> args) {
		if (args.size() > 0 && message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			String enableMode = args.get(0);
			String targetName = args.get(1);
			if (targetName.matches("enable|disable"))
				message.getChannel().sendMessage("This command is not disabled.").queue();
			else {
				if (enableMode.equals("command")) {
					AbstractCommand foundCommand = null;

					for (AbstractCommand command : commands) {
						if (command.getName().equals(targetName) || Arrays.asList(command.getCommandAliases()).contains(targetName)) {
							foundCommand = command;
							targetName = command.getName();
							break;
						}
					}

					if (foundCommand == null)
						message.getChannel().sendMessage("No command was found with name `" + targetName + "`").queue();
					else {
						Server server = serverService.getServer(message.getGuild().getId());
						if (!server.getCommandBlacklist().contains(targetName)) {
							message.getChannel().sendMessage("This command is not disabled!").queue();
						} else {
							server.removeFromCommandBlacklist(targetName);
							serverService.saveServer(server);
							message.getChannel().sendMessage("Command has been enabled: " + targetName).queue();
						}
					}
				}
				else if (enableMode.equals("routine")) {
					AbstractRoutine foundRoutine = null;
					
					for (AbstractRoutine routine: routines) {
						if (routine.getFormattedName().equals(targetName)) {
							foundRoutine = routine;
							break;
						}
					}
					if (foundRoutine == null) message.getChannel().sendMessage("No Routine was found with name `" + targetName + "`").queue();
					else if (!foundRoutine.canBeDisabled()) message.getChannel().sendMessage("This routine:  `"+ targetName + "` can't be disabled/enabled. ").queue();
					else {
						Server server = serverService.getServer(message.getGuild().getId());
						if (!server.getRoutineBlacklist().contains(targetName)) {
							message.getChannel().sendMessage("This routine is not disabled!").queue();
						}
						else {
							server.removeFromRoutineBlacklist(targetName);
							serverService.saveServer(server);
							message.getChannel().sendMessage("Routine has been enabled: "+ targetName).queue();
						}
					}
				}
				else {
					message.getChannel().sendMessage(enableMode + ": is not a valid mode.").queue();
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "enables a command or routine";
	}

	@Override
	public String getUsage() {
		return "enable routine ChatRoutine\n"+"enable command ping\n";
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
		String[] aliases = {"unblock"};
		return aliases;
	}

}
