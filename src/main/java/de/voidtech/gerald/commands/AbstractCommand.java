package main.java.de.voidtech.gerald.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand 
{
	private Message message;
	
	public final void execute(Message message, List<String> args) throws Exception
	{
		this.message = message;
		
		executeInternal(message, args);
	}
	
	public abstract void executeInternal(Message message, List<String> args) throws Exception;
	
	public abstract String getDescription();
	public abstract String getUsage();
	
	public void sendErrorOccurred()
	{
		this.message.getChannel().sendMessageFormat("```An error has occurred while executing your command```");
	}
}
