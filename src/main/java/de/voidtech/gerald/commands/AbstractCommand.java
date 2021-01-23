package main.java.de.voidtech.gerald.commands;

import java.util.List;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand implements Runnable {
	private Message message;
	private List<String> args;
	private EventWaiter waiter;

	public AbstractCommand() {}

	public AbstractCommand(String s) {}

	public void initCommand(Message message, List<String> args, EventWaiter waiter)

	public AbstractCommand() {}

	public AbstractCommand(String s) {}
	{
		this.message = message;
		this.args = args;
		this.waiter = waiter;
	}
	
	public void run() {
		executeInternal(message, args);
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
