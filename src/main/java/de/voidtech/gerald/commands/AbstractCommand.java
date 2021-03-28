package main.java.de.voidtech.gerald.commands;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.ThreadManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand{
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	ThreadManager threadManager;
	
	private void runCommandInThread(Message message, List<String> args) {
		Runnable commandThreadRunnable = new Runnable() {
			public void run() {
				executeInternal(message, args);
			}
		};
		threadManager.getThreadByName("T-Command").execute(commandThreadRunnable);
	}

	public void run(Message message, List<String> args) {
		if (message.getChannel().getType() == ChannelType.PRIVATE) {
			runCommandInThread(message, args);
		} else {
			Server server = serverService.getServer(message.getGuild().getId());
			Set<String> channelWhitelist = server.getChannelWhitelist();
			Set<String> commandBlacklist = server.getCommandBlacklist();
			
			boolean channelWhitelisted = channelWhitelist.isEmpty() || (channelWhitelist.contains(message.getChannel().getId()));
			boolean commandOnBlacklist = commandBlacklist.contains(getName());
			
			if((channelWhitelisted && !commandOnBlacklist) || message.getMember().hasPermission(Permission.ADMINISTRATOR))
			{
				runCommandInThread(message, args);
			}	
		}
	}
	
	public abstract void executeInternal(Message message, List<String> args);

	public abstract String getDescription();

	public abstract String getUsage();
	    
	public abstract String getName();

	public abstract CommandCategory getCommandCategory();

	public abstract boolean isDMCapable();

	public abstract boolean requiresArguments();
	
}
