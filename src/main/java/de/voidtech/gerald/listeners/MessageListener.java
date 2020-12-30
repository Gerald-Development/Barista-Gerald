package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.service.MessageHandler;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class MessageListener implements EventListener {

	private MessageHandler msgHandler;

	public MessageListener()
	{
		this.msgHandler = MessageHandler.getInstance();
	}
	
	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof MessageReceivedEvent) 
		{
			MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
			
			msgHandler.handleMessage(msgEvent.getMessage());
		}
	}

}
