package main.java.de.voidtech.gerald.commands.management;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@Command
public class EnableCommand extends AbstractCommand {

	@Autowired
	private List<AbstractCommand> commands;

	@Autowired
	private ServerService serverService;

	@Override
	public void executeInternal(Message message, List<String> args) {
		if (args.size() > 0 && message.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			String commandName = args.get(0);
			if (commandName.matches("enable|disable"))
				message.getChannel().sendMessage("This command is not disabled.").queue();
			else {
				AbstractCommand foundCommand = null;

				for (AbstractCommand command : commands) {
					if (command.getName().equals(commandName)) {
						foundCommand = command;
						break;
					}
				}

				if (foundCommand == null)
					message.getChannel().sendMessage("No command was found with name `" + commandName + "`").queue();
				else {
					Server server = serverService.getServer(message.getGuild().getId());
					if (!server.getCommandBlacklist().contains(commandName)) {
						message.getChannel().sendMessage("This command is not disabled!").queue();
					} else {
						server.removeFromCommandBlacklist(commandName);
						serverService.saveServer(server);
						message.getChannel().sendMessage("Command has been enabled: " + commandName).queue();
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "enables a command";
	}

	@Override
	public String getUsage() {
		return "enable ping";
	}

	@Override
	public String getName() {
		return "enable";
	}

}
