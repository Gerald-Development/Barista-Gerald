package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.service.SuggestionService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteListener implements EventListener {
	
	@Autowired
	private SuggestionService suggestionService;
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof MessageReactionAddEvent) {
			MessageReactionAddEvent reaction = (MessageReactionAddEvent) event;
			suggestionService.handleVote(reaction);
		}
	}
}