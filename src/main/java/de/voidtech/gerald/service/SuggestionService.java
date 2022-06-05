package main.java.de.voidtech.gerald.service;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
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
    private SessionFactory sessionFactory;
    
    @Autowired
    private ServerService serverService;
    
    private HashMap<String, Color> getValidAdminEmotes() {
    	HashMap<String, Color> adminEmotes = new HashMap<String, Color>();
    	adminEmotes.put("U+1f7e3", new Color(168, 144, 216, 255));
    	adminEmotes.put("U+1f535", new Color(90, 175, 240, 255));
    	adminEmotes.put("U+1f7e2", new Color(124, 176, 86, 255));
    	adminEmotes.put("U+1f7e0", new Color(241, 141, 0, 255));
    	adminEmotes.put("U+1f534", new Color(217, 41, 64, 255));
    	return adminEmotes;
    }
	
    public SuggestionChannel getSuggestionChannel(long serverID) {
        try (Session session = sessionFactory.openSession()) {
            SuggestionChannel suggestionChannel = (SuggestionChannel) session.createQuery("FROM SuggestionChannel WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
            return suggestionChannel;
        }
    }
    
    public void deleteSuggestionChannel(long guildID) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.createQuery("DELETE FROM SuggestionChannel WHERE ServerID = :guildID")
                    .setParameter("guildID", guildID)
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }
    
    public void saveSuggestionChannel(SuggestionChannel config) {
    	try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(config);
            session.getTransaction().commit();
        }
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
		Color emoteColour = getValidAdminEmotes().get(reaction.getReactionEmote().getAsCodepoints());
		//If reaction is not a coloured dot, ignore
		if (emoteColour == null) return;
		//If message is not a suggestion channel embed, ignore
		Message message = reaction.getChannel().retrieveMessageById(reaction.getMessageId()).complete();
		if (!message.getAuthor().getId().equals(reaction.getJDA().getSelfUser().getId())) return;;
		if (message.getEmbeds().isEmpty()) return;
		if (!message.getEmbeds().get(0).getTitle().equals("New Suggestion!")) return;
		//Edit embed colour and remove reaction
		EmbedBuilder suggestionModifier = new EmbedBuilder(message.getEmbeds().get(0));
		suggestionModifier.setColor(emoteColour);
		updateReviewedField(suggestionModifier, reaction);
		message.editMessageEmbeds(suggestionModifier.build()).complete();
		reaction.getReaction().removeReaction(reaction.getUser()).queue();
	}
	
	private void updateReviewedField(EmbedBuilder suggestionModifier, GuildMessageReactionAddEvent reaction) {
		List<Field> fields = suggestionModifier.getFields();
		Field suggestionField = fields.stream().filter(f -> f.getName().equals("Suggestion")).findFirst().orElse(null);
		suggestionModifier.clearFields();
		suggestionModifier.addField(suggestionField);
		suggestionModifier.addField("Last Reviewed By", reaction.getUser().getAsMention(), false);
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
