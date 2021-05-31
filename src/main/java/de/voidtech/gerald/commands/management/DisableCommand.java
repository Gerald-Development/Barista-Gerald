package main.java.de.voidtech.gerald.commands.management;

import java.util.Arrays;
import java.util.List;

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
	private ServerService serverService;

	@Override
	public void executeInternal(Message message, List<String> args) {
		if (args.size() > 0 && message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			String commandName = args.get(0);
			if (commandName.matches("enable|disable"))
				message.getChannel().sendMessage("You cannot disable this command.").queue();
			else {
				AbstractCommand foundCommand = null;

				for (AbstractCommand command : commands) {
					if (command.getName().equals(commandName) || Arrays.asList(command.getCommandAliases()).contains(commandName)) {
						foundCommand = command;
						commandName = command.getName();
						break;
					}
				}

				if (foundCommand == null)
					message.getChannel().sendMessage("No command was found with name `" + commandName + "`").queue();
				else {
					Server server = serverService.getServer(message.getGuild().getId());
					if (server.getCommandBlacklist().contains(commandName)) {
						message.getChannel().sendMessage("This command is already disabled!");
					} else {
						server.addToCommandBlacklist(commandName);
						serverService.saveServer(server);
						message.getChannel().sendMessage("Command has been disabled: " + commandName).queue();
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "disabled a command";
	}

	@Override
	public String getUsage() {
		return "disable ping";
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
