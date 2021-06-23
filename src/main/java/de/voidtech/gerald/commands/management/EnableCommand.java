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
		if (message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			String targetName = args.get(0).toLowerCase();
			if (targetName.startsWith("r-")) {
				AbstractRoutine foundRoutine = null;
				
				for (AbstractRoutine routine: routines) {
					if (routine.getName().equals(targetName)) {
						foundRoutine = routine;
						break;
					}
				}
				if (foundRoutine == null) message.getChannel().sendMessage("**No Routine was found with name `" + targetName + "`**").queue();
				else if (!foundRoutine.canBeDisabled()) message.getChannel().sendMessage("**Routine `"+ targetName + "` cannot be disabled/enabled!**").queue();
				else {
					Server server = serverService.getServer(message.getGuild().getId());
					if (!server.getRoutineBlacklist().contains(targetName))
						message.getChannel().sendMessage("**This routine is not disabled!**").queue();
					else {
						server.removeFromRoutineBlacklist(targetName);
						serverService.saveServer(server);
						message.getChannel().sendMessage("**Routine `" + targetName + "`has been enabled!**").queue();
					}
				}
			}
			else {
				AbstractCommand foundCommand = null;
				for (AbstractCommand command : commands) {
					if (command.getName().equals(targetName) || Arrays.asList(command.getCommandAliases()).contains(targetName)) {
						foundCommand = command;
						targetName = command.getName();
						break;
					}
				}
				if (foundCommand == null)
					message.getChannel().sendMessage("**No command was found with name `" + targetName + "`**").queue();
				else if (!foundCommand.canBeDisabled()) message.getChannel().sendMessage("**The command `"+ targetName + "` cannot be disabled/enabled!**").queue();
				else {
					Server server = serverService.getServer(message.getGuild().getId());
					if (!server.getCommandBlacklist().contains(targetName)) {
						message.getChannel().sendMessage("**This command is not disabled!**").queue();
					} else {
						server.removeFromCommandBlacklist(targetName);
						serverService.saveServer(server);
						message.getChannel().sendMessage("**Command `" + targetName + "` has been enabled!**").queue();
					}
				}
			}
		}
	}
	
	@Override
	public String getDescription() {
		return "Allows you to enable a command or routine! Note: Some routines or commands cannot be disabled. Routine names always use the format r-[name]";
	}

	@Override
	public String getUsage() {
		return "enable r-nitrolite\n"
				+"enable ping";
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
		String[] aliases = {"unlock"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return false;
	}

}
