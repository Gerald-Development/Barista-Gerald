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
public class DisableCommand extends AbstractCommand {

	@Autowired
	private List<AbstractCommand> commands;

	@Autowired
	private List<AbstractRoutine> routines;

	@Autowired
	private ServerService serverService;

	@Override
	public void executeInternal(Message message, List<String> args) {
		if (args.size() > 0 && message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			String disableMode = args.get(0);
			String targetName = args.get(1);
			if (targetName.matches("enable|disable"))
				message.getChannel().sendMessage("You cannot disable this command.").queue();
			else {
				if (disableMode.equals("command")) {
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
						if (server.getCommandBlacklist().contains(targetName)) {
							message.getChannel().sendMessage("This command is already disabled!").queue();
						} else {
							server.addToCommandBlacklist(targetName);
							serverService.saveServer(server);
							message.getChannel().sendMessage("Command has been disabled: " + targetName).queue();
						}
					}
				}
				else if (disableMode.equals("routine")) {
					AbstractRoutine foundRoutine = null;

					for (AbstractRoutine routine: routines) {
						if (routine.getFormattedName().equals(targetName)) {
							foundRoutine = routine;
							break;
						}
					}
					if (foundRoutine == null) message.getChannel().sendMessage("No Routine was found with name `" + targetName + "`").queue();
					else {
						Server server = serverService.getServer(message.getGuild().getId());
						if (server.getRoutineBlacklist().contains(targetName)) {
							message.getChannel().sendMessage("This routine is not enabled!").queue();
						}
						else {
							server.addToRoutineBlacklist(targetName);
							serverService.saveServer(server);
							message.getChannel().sendMessage("Routine has been disabled: "+ targetName).queue();
						}
					}
				}
				else {
					message.getChannel().sendMessage(disableMode + ": is not a valid mode.").queue();
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "disabled a command or routine";
	}

	@Override
	public String getUsage() {
		return "disable routine ChatRoutine\n"+"disable command ping\n";
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
		String[] aliases = {"block"};
		return aliases;
	}

}
