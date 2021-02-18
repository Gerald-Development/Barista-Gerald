package main.java.de.voidtech.gerald.commands.management;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@Command
public class PrefixCommand extends AbstractCommand {

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private GeraldConfig config;
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if(message.getMember().hasPermission(Permission.MANAGE_SERVER))
		{
			Server server = serverService.getServer(message.getGuild().getId());
			
			if(args.size() > 0)
			{
				String prefix = args.get(0);
				
				server.setPrefix(prefix);
				serverService.saveServer(server);
				
				message.getChannel().sendMessage(String.format("Prefix was changed to `%s`", prefix)).queue();
			}
			else {
				server.setPrefix(config.getDefaultPrefix());
				serverService.saveServer(server);
				message.getChannel().sendMessage(String.format("Prefix has been reset to `%s`", config.getDefaultPrefix())).queue();
			}
		}
	}

	@Override
	public String getDescription() {
		return "Set the prefix of Gerald!";
	}

	@Override
	public String getUsage() {
		return "prefix %";
	}

	@Override
	public String getName() {
		return "prefix";
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
		return false;
	}

}
