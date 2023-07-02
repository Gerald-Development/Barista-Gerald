package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.persistence.entity.StarboardConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.StarboardService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StarboardListener implements EventListener {

	private static final String STAR_UNICODE = "U+2b50";
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private StarboardService starboardService;
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof MessageReactionAddEvent) {
			MessageReactionAddEvent reaction = (MessageReactionAddEvent) event;
			if (reaction.getEmoji().asUnicode().getAsCodepoints().equals(STAR_UNICODE)) {
				
				long serverID = serverService.getServer(reaction.getGuild().getId()).getId();
				
				StarboardConfig config = starboardService.getStarboardConfig(serverID);
				if (config != null) {
					pinStarIfAllowed(reaction, config, serverID);
				}
			}
		}	
	}

	private void pinStarIfAllowed(MessageReactionAddEvent reaction, StarboardConfig config, long serverID) {
		if (!starboardService.reactionIsInStarboardChannel(reaction.getChannel().getId(), serverID)) {
			if (config.getIgnoredChannels() != null) {
				if (!config.getIgnoredChannels().contains(reaction.getChannel().getId()))
					starboardService.checkStars(serverID, reaction);	
			} else starboardService.checkStars(serverID, reaction);
		}
	}
}