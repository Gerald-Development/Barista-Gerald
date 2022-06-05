package main.java.de.voidtech.gerald.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.java.de.voidtech.gerald.service.SuggestionService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class VoteListener implements EventListener {
	
	@Autowired
	private SuggestionService suggestionService;
	
	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof GuildMessageReactionAddEvent) {
			GuildMessageReactionAddEvent reaction = (GuildMessageReactionAddEvent) event;
			suggestionService.handleVote(reaction);
		}
	}
}