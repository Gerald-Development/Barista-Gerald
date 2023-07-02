package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelDeleteListener implements EventListener {

	@Autowired
	private ServerService serverService;
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if(event instanceof ChannelDeleteEvent)
		{
			ChannelUnion channel = ((ChannelDeleteEvent) event).getChannel();
			Server server = serverService.getServer(((ChannelDeleteEvent) event).getGuild().getId());
			
			server.removeFromChannelWhitelist(channel.getId());
			serverService.saveServer(server);
		}
	}
}