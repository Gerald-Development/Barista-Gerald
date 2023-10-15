package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.service.MessageService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@Listener
public class MessageListener extends ListenerAdapter {

	@Autowired
	private MessageService msgHandler;

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		msgHandler.handleMessage(event.getMessage());
	}

}