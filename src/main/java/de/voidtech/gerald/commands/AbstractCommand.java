package main.java.de.voidtech.gerald.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand implements Runnable {
	private Message message;
	private List<String> args;

	public void initCommand(Message message, List<String> args)
	{
		this.message = message;
		this.args = args;
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
}
