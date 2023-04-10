package main.java.de.voidtech.gerald.service;

import java.util.List;
import java.util.Objects;

import main.java.de.voidtech.gerald.entities.SuggestionChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
import main.java.de.voidtech.gerald.entities.SuggestionEmote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

@Service
public class SuggestionService {
	
    @Autowired
    private SuggestionChannelRepository repository;
    
    @Autowired
    private ServerService serverService;
	
    public SuggestionChannel getSuggestionChannel(long serverID) {
        return repository.getChannelByServerId(serverID);
    }
    
    public void deleteSuggestionChannel(long guildID) {
        repository.deleteSuggestionChannel(guildID);
    }
    
    public void saveSuggestionChannel(SuggestionChannel config) {
    	repository.save(config);
    }
    
    public boolean isGuildChannel(String channelID, CommandContext context) {
        return context.getGuild().getTextChannelById(channelID) != null;
    }
    
    public boolean isRole(String roleID, CommandContext context) {
    	return context.getGuild().getRoleById(roleID) != null;
    }

	public boolean memberHasRole(Member member, String roleID) {
		Role role = member.getRoles().stream()
				.filter(searchRole -> searchRole.getId().equals(roleID))
				.findFirst()
				.orElse(null);
		return role != null;
	}
	
	private void handleUserVote(GuildMessageReactionAddEvent reaction, SuggestionChannel config) {
		//If no vote role is required, ignore
		if (!config.voteRoleRequired()) return;
		//If member does not have vote role, delete
		boolean hasRole = memberHasRole(reaction.getMember(), config.getVoteRoleID());
		if (!hasRole) reaction.getReaction().removeReaction(reaction.getUser()).queue();
	}

	private void handleAdminVote(GuildMessageReactionAddEvent reaction, SuggestionChannel config) {
		//If member does not have "manage messages" perms ignore
		if (!reaction.getMember().getPermissions().contains(Permission.MESSAGE_MANAGE)) return;
		SuggestionEmote emote = SuggestionEmote.GetEmoteFromUnicode(reaction.getReactionEmote().getAsCodepoints());
		//If reaction is not a coloured dot, ignore
		if (emote == null) return;
		//If message is not a suggestion channel embed, ignore
		Message message = reaction.getChannel().retrieveMessageById(reaction.getMessageId()).complete();
		if (!message.getAuthor().getId().equals(reaction.getJDA().getSelfUser().getId())) return;
		if (message.getEmbeds().isEmpty()) return;
		if (!Objects.requireNonNull(message.getEmbeds().get(0).getTitle()).equals("New Suggestion!")) return;
		//Edit embed colour and remove reaction
		EmbedBuilder suggestionModifier = new EmbedBuilder(message.getEmbeds().get(0));
		updateEmbed(suggestionModifier, reaction, emote);
		message.editMessageEmbeds(suggestionModifier.build()).complete();
		reaction.getReaction().removeReaction(reaction.getUser()).queue();
	}
	
	private void updateEmbed(EmbedBuilder suggestionModifier, GuildMessageReactionAddEvent reaction, SuggestionEmote emote) {
		List<Field> fields = suggestionModifier.getFields();
		Field suggestionField = fields.stream().filter(f -> Objects.requireNonNull(f.getName()).equals("Suggestion")).findFirst().orElse(null);
		suggestionModifier.clearFields()
			.addField(suggestionField)
			.addField("Last Reviewed By", reaction.getUser().getAsMention() + " " + emote.getEmote(), false)
			.setColor(emote.getColour());
	}

	public void handleVote(GuildMessageReactionAddEvent reaction) {
		Server server = serverService.getServer(reaction.getGuild().getId());
		SuggestionChannel config = getSuggestionChannel(server.getId());
		//Ignore bot reactions
		if (reaction.getMember().getId().equals(reaction.getJDA().getSelfUser().getId())) return;
		//If no suggestion config has been set up, ignore
		if (config == null) return;
		//If reaction is not added in vote channel ignore
		if (!config.getSuggestionChannel().equals(reaction.getChannel().getId())) return;
		//If reaction is NOT unicode, ignore
		if (reaction.getReactionEmote().isEmote()) return;
		
		handleUserVote(reaction, config);
		handleAdminVote(reaction, config);
	}
}