package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.service.MessageHandler;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageListener implements EventListener {

	@Autowired
	private MessageHandler msgHandler;

	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof MessageReceivedEvent) 
		{
			MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
			
			msgHandler.handleMessage(msgEvent.getMessage());
		}
	}

}