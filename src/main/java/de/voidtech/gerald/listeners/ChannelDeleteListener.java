package main.java.de.voidtech.gerald.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class ChannelDeleteListener implements EventListener {

	@Autowired
	private ServerService serverService;
	
	@Override
	public void onEvent(GenericEvent event) {
		if(event instanceof TextChannelDeleteEvent)
		{
			TextChannel channel = ((TextChannelDeleteEvent) event).getChannel();
			Server server = serverService.getServer(((TextChannelDeleteEvent) event).getGuild().getId());
			
			server.removeFromChannelWhitelist(channel.getId());
			serverService.saveServer(server);
		}
	}
}
