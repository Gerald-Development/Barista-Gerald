package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.service.SuggestionService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class VoteListener extends ListenerAdapter {

    @Autowired
    private SuggestionService suggestionService;

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getEmoji().getType().equals(Emoji.Type.UNICODE)) return;
        suggestionService.handleVote(event);
    }
}