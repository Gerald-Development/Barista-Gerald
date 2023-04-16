package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.persistence.entity.StarboardConfig;
import main.java.de.voidtech.gerald.persistence.entity.StarboardMessage;
import main.java.de.voidtech.gerald.persistence.repository.StarboardConfigRepository;
import main.java.de.voidtech.gerald.persistence.repository.StarboardMessageRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
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
	private StarboardConfigRepository configRepository;

	@Autowired
	private StarboardMessageRepository messageRepository;
	
	private static final String STAR_UNICODE = "U+2b50";
 
	public StarboardConfig getStarboardConfig(long serverID) {
		return configRepository.getConfigByServerID(serverID);
	}
 
	public void deleteStarboardConfig(CommandContext context, Server server) {
		configRepository.deleteConfigByServerID(server.getId());
		context.getChannel().sendMessage("**The Starboard has been disabled. You will need to run setup again if you wish to undo this! Your starred messages will not be lost.**").queue();
	}

	public void updateConfig(StarboardConfig config) {
		configRepository.save(config);
	}
 
	public void completeStarboardSetup(CommandContext context, String channelID, String starCount, Server server) {
		int requiredStarCount = Integer.parseInt(starCount);
		StarboardConfig config = new StarboardConfig(server.getId(), channelID, requiredStarCount, null);
		configRepository.save(config);
		context.getChannel().sendMessageEmbeds(createSetupEmbed(config)).queue();
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
		return configRepository.getConfigIfServerIdAndChannelIdMatch(serverID, channelID) != null;
	}
	
	private StarboardMessage getStarboardMessage(long serverID, String messageID) {
		return messageRepository.getMessageByIdAndServerID(serverID, messageID);
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
		messageRepository.save(new StarboardMessage(originMessageID, selfMessageID, serverID));
	}
	
	private void editStarboardMessage(StarboardMessage starboardMessage, Message message, long starCountFromMessage, StarboardConfig config) {
		Message selfMessage = message.getJDA()
				.getTextChannelById(config.getChannelID())
				.retrieveMessageById(starboardMessage.getSelfMessageID())
				.complete();
		selfMessage.editMessage(":star: **" + starCountFromMessage + "**").queue();
	}

	private void sendOrUpdateMessage(long serverID, GuildMessageReactionAddEvent reaction, StarboardConfig config, Message message, long starCountFromMessage) {
		StarboardMessage starboardMessage = getStarboardMessage(serverID, reaction.getMessageId());
		
		if (starboardMessage == null) {
			MessageEmbed starboardEmbed = constructEmbed(message);
			message.getJDA().getTextChannelById(config.getChannelID()).sendMessageEmbeds(starboardEmbed).queue(sentMessage -> {
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