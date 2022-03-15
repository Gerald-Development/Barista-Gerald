package main.java.de.voidtech.gerald.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.SuggestionService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class VoteListener implements EventListener {

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private SuggestionService suggestionService;
	
	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof GuildMessageReactionAddEvent) {
			GuildMessageReactionAddEvent reaction = (GuildMessageReactionAddEvent) event;
			Server server = serverService.getServer(reaction.getGuild().getId());
			SuggestionChannel config = suggestionService.getSuggestionChannel(server.getId());
			
			if (reaction.getMember().getId().equals(reaction.getJDA().getSelfUser().getId())) return;
			if (config == null) return;
			if (!config.getSuggestionChannel().equals(reaction.getChannel().getId())) return;
			if (!config.voteRoleRequired()) return;
			boolean hasRole = suggestionService.memberHasRole(reaction.getMember(), config.getVoteRoleID());
			if (!hasRole) reaction.getReaction().removeReaction(reaction.getUser()).queue();	
		}
	}
}