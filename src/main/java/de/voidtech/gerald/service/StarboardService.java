package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.StarboardConfig;
import main.java.de.voidtech.gerald.entities.StarboardMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class StarboardService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private static final String STAR_UNICODE = "U+2b50";
 
	public StarboardConfig getStarboardConfig(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			return (StarboardConfig) session.createQuery("FROM StarboardConfig WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
		}	
	}
 
	public void deleteStarboardConfig(CommandContext context, Server server) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM StarboardConfig WHERE ServerID = :serverID")
				.setParameter("serverID", server.getId())
				.executeUpdate();
			session.getTransaction().commit();
			context.getChannel().sendMessage("**The Starboard has been disabled. You will need to run setup again if you wish to undo this! Your starred messages will not be lost.**").queue();
		}
	}

	public void updateConfig(StarboardConfig config) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();			
			session.saveOrUpdate(config);
			session.getTransaction().commit();
		}		
	}
 
	public void completeStarboardSetup(CommandContext context, String channelID, String starCount, Server server) {
		int requiredStarCount = Integer.parseInt(starCount);
		
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			StarboardConfig config = new StarboardConfig(server.getId(), channelID, requiredStarCount, null);
			
			session.saveOrUpdate(config);
			session.getTransaction().commit();
			
			context.getChannel().sendMessageEmbeds(createSetupEmbed(config)).queue();
		}
	}
	
	private MessageEmbed createSetupEmbed(StarboardConfig config) {
		EmbedBuilder setupEmbedBuilder = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Setup complete!")
				.setDescription("**Channel: <#" + config.getChannelID() + ">**\n"
					+ "**Stars required:** " + config.getRequiredStarCount() + "\n"
					+ "Users must react with the :star: emote to star a message!");
		return setupEmbedBuilder.build();
	}

	//This method may look like something else, but it is used to check if the channel a star is added to is a starboard channel or not
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
			return (StarboardMessage) session.createQuery("FROM StarboardMessage WHERE serverID = :serverID AND originMessageID = :messageID")
                    .setParameter("serverID", serverID)
                    .setParameter("messageID", messageID)
                    .uniqueResult();
		}
	}
	
	private MessageEmbed constructEmbed(Message message) {
		EmbedBuilder starboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setAuthor(message.getAuthor().getAsTag(), GlobalConstants.LINKTREE_URL, message.getAuthor().getAvatarUrl())
				.setDescription(message.getContentRaw())
				.setTitle("Jump to message!", message.getJumpUrl())
				.setTimestamp(Instant.now())
				.setFooter("in #" + message.getChannel().getName());
		
		boolean found = false;
		if (message.getAttachments().size() > 0) {
			
			for (Attachment attachment : message.getAttachments()) {
				if (attachment.isImage() && !found) {
					starboardEmbed.setImage(attachment.getUrl());
					found = true;
				}
			}
		}
		if (!found && !message.getEmbeds().isEmpty()) {
			for (MessageEmbed embed : message.getEmbeds()) {
				if (embed.getImage() != null && !found) {
					starboardEmbed.setImage(embed.getImage().getUrl());
					found = true;
				}
			}
		}
		
		return starboardEmbed.build();
	}
	
	private synchronized void persistMessage(String originMessageID, String selfMessageID, long serverID) {		
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			StarboardMessage message = new StarboardMessage(originMessageID, selfMessageID, serverID);
			session.saveOrUpdate(message);
			session.getTransaction().commit();
		}
	}
	
	private void editStarboardMessage(StarboardMessage starboardMessage, Message message, long starCountFromMessage, StarboardConfig config) {
		Message selfMessage = Objects.requireNonNull(message.getJDA()//
						.getTextChannelById(config.getChannelID()))//
		 .retrieveMessageById(starboardMessage.getSelfMessageID())//
		 .complete();
		selfMessage.editMessage(":star: **" + starCountFromMessage + "**").queue();
	}

	private void sendOrUpdateMessage(long serverID, GuildMessageReactionAddEvent reaction, StarboardConfig config, Message message, long starCountFromMessage) {
		StarboardMessage starboardMessage = getStarboardMessage(serverID, reaction.getMessageId());
		
		if (starboardMessage == null) {
			MessageEmbed starboardEmbed = constructEmbed(message);
			Objects.requireNonNull(message.getJDA().getTextChannelById(config.getChannelID())).sendMessageEmbeds(starboardEmbed).queue(sentMessage -> {
				sentMessage.editMessage(":star: **" + starCountFromMessage + "**").queue();
				persistMessage(message.getId(), sentMessage.getId(), serverID);
			});
		} else editStarboardMessage(starboardMessage, message, starCountFromMessage, config);
	}

	private long getStarsFromMessage(Message message) {
		long count = message.getReactions()
				 .stream()
				 .filter(reaction -> reaction.getReactionEmote().toString().equals("RE:" + STAR_UNICODE))
				 .count();
		for (MessageReaction reaction: message.getReactions()) {
			if (reaction.getReactionEmote().toString().equals("RE:" + STAR_UNICODE)) {
				count = reaction.getCount();
			}
		}
		return count;
	}
	
	public void checkStars(long serverID, GuildMessageReactionAddEvent reaction) {
		StarboardConfig config = getStarboardConfig(serverID);
		Message message = reaction.getChannel().retrieveMessageById(reaction.getMessageId()).complete();
		
		long starCountFromMessage = getStarsFromMessage(message);
		
		if (starCountFromMessage >= config.getRequiredStarCount()) {
			sendOrUpdateMessage(serverID, reaction, config, message, starCountFromMessage);
		}
	}

	public List<String> getIgnoredChannels(long serverID) {
		StarboardConfig config = getStarboardConfig(serverID);
		return config.getIgnoredChannels();
	}

	public void addChannelToIgnorelist(long serverID, String channelID) {
		List<String> ignoredChannels = getIgnoredChannels(serverID);
		List<String> newIgnoredChannelList;
		if (ignoredChannels == null) {
			newIgnoredChannelList = new ArrayList<>();
		} else {
			newIgnoredChannelList = new ArrayList<>(ignoredChannels);
		}
		newIgnoredChannelList.add(channelID);
		StarboardConfig config = getStarboardConfig(serverID);
		config.setIgnoredChannels(newIgnoredChannelList);
		updateConfig(config);
	}

	public void removeFromIgnorelist(long serverID, String channelID) {
		List<String> ignoredChannels = getIgnoredChannels(serverID);
		List<String> newIgnoredChannelList = new ArrayList<>(ignoredChannels);
		newIgnoredChannelList.remove(channelID);
		if (newIgnoredChannelList.size() == 0)
			newIgnoredChannelList = null;
		StarboardConfig config = getStarboardConfig(serverID);
		config.setIgnoredChannels(Objects.requireNonNull(newIgnoredChannelList));
		updateConfig(config);
	}
}