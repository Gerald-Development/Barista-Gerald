package main.java.de.voidtech.gerald.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.StarboardConfig;
import main.java.de.voidtech.gerald.entities.StarboardMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

@Service
public class StarboardService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private static final String STAR_UNICODE = " U+2B50";

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
	
	private void sendOrUpdateMessage(long serverID, GuildMessageReactionAddEvent reaction, StarboardConfig config) {
		StarboardMessage starboardMessage = getStarboardMessage(serverID, reaction.getMessageId());
		
		if (starboardMessage == null) {
			//method to construct embed
			//send message with star count
			//add message to DB
		} else {
			//edit message
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
			sendOrUpdateMessage(serverID, reaction, config);
		}
	}
}
