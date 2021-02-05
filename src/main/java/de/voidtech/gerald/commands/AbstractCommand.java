package main.java.de.voidtech.gerald.commands;

import java.util.List;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand implements Runnable {
	private Message message;
	private List<String> args;
	private EventWaiter waiter;
	private ServerService serverService;

	public AbstractCommand() {}

	public AbstractCommand(String s) {}

	public void initCommand(Message message, List<String> args, EventWaiter waiter) {
		this.message = message;
		this.args = args;
		this.waiter = waiter;
		this.serverService = ServerService.getInstance();
	}

	public void run() {
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
	
	public void sendErrorOccurred() {
		this.message.getChannel().sendMessageFormat("```An error has occurred while executing your command```");
	}
	
	public EventWaiter getEventWaiter() {
		return this.waiter;
	}
}
