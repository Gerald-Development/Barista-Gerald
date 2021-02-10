package main.java.de.voidtech.gerald.commands;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand{
	
	@Autowired
	private ServerService serverService;

	public void run(Message message, List<String> args) {
		Server server = serverService.getServer(message.getGuild().getId());
		List<String> channelWhitelist = server.getChannelWhitelist();
		
		if(channelWhitelist.isEmpty() || channelWhitelist.contains(message.getChannel().getId()) || message.getMember().hasPermission(Permission.ADMINISTRATOR))
		{
			executeInternal(message, args);
		}
	}
	
	public abstract void executeInternal(Message message, List<String> args);

	public abstract String getDescription();

	public abstract String getUsage();
	
	public abstract String getName();
	
}
