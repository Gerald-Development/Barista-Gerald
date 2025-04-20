package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.service.MessageService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class MessageListener extends ListenerAdapter {

	private final MessageService msgHandler;

	@Autowired
	public MessageListener(MessageService messageHandler) {
		this.msgHandler = messageHandler;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		msgHandler.handleMessage(event.getMessage());
	}

}