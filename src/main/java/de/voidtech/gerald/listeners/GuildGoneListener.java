package main.java.de.voidtech.gerald.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class GuildGoneListener implements EventListener {

	@Autowired
	private ServerService serverService;
	
	@Override
	public void onEvent(GenericEvent event) {
		if(event instanceof GuildLeaveEvent) {
			serverService.deleteServer(serverService.getServer(((GuildLeaveEvent) event).getGuild().getId()));
		}
	}
}
