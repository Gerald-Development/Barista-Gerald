package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.persistence.entity.StarboardConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.StarboardService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class StarboardListener extends ListenerAdapter {

    private static final String STAR_UNICODE = "U+2b50";

    @Autowired
    private ServerService serverService;

    @Autowired
    private StarboardService starboardService;

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getEmoji().getType().equals(Emoji.Type.UNICODE)) return;
        if (event.getEmoji().asUnicode().getAsCodepoints().equals(STAR_UNICODE)) {
            long serverID = serverService.getServer(event.getGuild().getId()).getId();
            StarboardConfig config = starboardService.getStarboardConfig(serverID);
            if (config != null) {
                pinStarIfAllowed(event, config, serverID);
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