package main.java.de.voidtech.gerald.service;

import java.awt.Color;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.entities.StarboardConfig;
import main.java.de.voidtech.gerald.entities.StarboardMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

@Service
public class StarboardService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private static final String STAR_UNICODE = "U+2b50";

	public boolean serverHasStarboard(long id) {
		try(Session session = sessionFactory.openSession())
		{
			StarboardConfig config = (StarboardConfig) session.createQuery("FROM StarboardConfig WHERE serverID = :serverID")
                    .setParameter("serverID", id)
                    .uniqueResult();
			return config != null;
		}
	}

	public StarboardConfig getConfig(long id) {
		try(Session session = sessionFactory.openSession())
		{
			StarboardConfig config = (StarboardConfig) session.createQuery("FROM StarboardConfig WHERE serverID = :serverID")
                    .setParameter("serverID", id)
                    .uniqueResult();
			return config;
		}
	}
	
	public boolean reactionIsInStarboardChannel(String channelID, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			StarboardConfig config = (StarboardConfig) session.createQuery("FROM StarboardConfig WHERE serverID = :serverID AND starboardChannel = :channelID")
                    .setParameter("serverID", serverID)
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			return config != null;
		}
	}
	
	private StarboardMessage getStarboardMessage(long serverID, String messageID) {
		try(Session session = sessionFactory.openSession())
		{
			StarboardMessage message = (StarboardMessage) session.createQuery("FROM StarboardMessage WHERE serverID = :serverID AND originMessageID = :messageID")
                    .setParameter("serverID", serverID)
                    .setParameter("messageID", messageID)
                    .uniqueResult();
			return message;
		}
	}
	
	private MessageEmbed constructEmbed(Message message) {
		EmbedBuilder starboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setAuthor(message.getAuthor().getAsTag(), GlobalConstants.LINKTREE_URL, message.getAuthor().getAvatarUrl())
				.setDescription(message.getContentRaw())
				.setTitle("Jump to message!", message.getJumpUrl());
		
		if (message.getAttachments().size() > 0) {
			starboardEmbed.setImage(message.getAttachments().get(0).getUrl());
		}
		
		return starboardEmbed.build();
	}
	
	private void persistMessage(String originMessageID, String selfMessageID, long serverID) {		
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			StarboardMessage message = new StarboardMessage(originMessageID, selfMessageID, serverID);
			session.saveOrUpdate(message);
			session.getTransaction().commit();
		}
	}
	
	private void editStarboardMessage(StarboardMessage starboardMessage, Message message, int starCountFromMessage, StarboardConfig config) {
		//I doubt this is the ugliest code in this service
		Message selfMessage = message.getJDA().getTextChannelById(config.getChannelID()).retrieveMessageById(starboardMessage.getSelfMessageID()).complete();
		selfMessage.editMessage(":star: **" + starCountFromMessage + "**").queue();
	}

	private void sendOrUpdateMessage(long serverID, GuildMessageReactionAddEvent reaction, StarboardConfig config, Message message, int starCountFromMessage) {
		StarboardMessage starboardMessage = getStarboardMessage(serverID, reaction.getMessageId());
		
		if (starboardMessage == null) {
			MessageEmbed starboardEmbed = constructEmbed(message);
			message.getJDA().getTextChannelById(config.getChannelID()).sendMessage(starboardEmbed).queue(sentMessage -> {
				sentMessage.editMessage(":star: **" + starCountFromMessage + "**").queue();
				persistMessage(message.getId(), sentMessage.getId(), serverID);
			});
		} else {
			editStarboardMessage(starboardMessage, message, starCountFromMessage, config);
		}
	}

	private int getStarsFromMessage(Message message) {
		int count = 0;
		for (MessageReaction reaction: message.getReactions()) {
			if (reaction.getReactionEmote().toString().equals("RE:" + STAR_UNICODE)) {
				count = reaction.getCount();
			}
		}
		return count;
	}
	
	public void checkStars(long serverID, GuildMessageReactionAddEvent reaction) {
		StarboardConfig config = getConfig(serverID);
		Message message = reaction.getChannel().retrieveMessageById(reaction.getMessageId()).complete();
		
		int starCountFromMessage = getStarsFromMessage(message);
		
		if (starCountFromMessage >= config.getRequiredStarCount()) {
			sendOrUpdateMessage(serverID, reaction, config, message, starCountFromMessage);
		}
	}
}
