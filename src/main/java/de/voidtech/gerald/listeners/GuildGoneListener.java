package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuildGoneListener implements EventListener {

	@Autowired
	private ServerService serverService;
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if(event instanceof GuildLeaveEvent) {
			serverService.deleteServer(serverService.getServer(((GuildLeaveEvent) event).getGuild().getId()));
		}
	}
}