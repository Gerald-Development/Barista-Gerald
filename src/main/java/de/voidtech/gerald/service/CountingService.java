package main.java.de.voidtech.gerald.service;

import java.awt.Color;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.CountingChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class CountingService {
	
	private final static String INCORRECT = "U+274C";

	@Autowired
	public SessionFactory sessionFactory;	
	
	public CountingChannel getCountingChannel (String channelID) {
		CountingChannel dbChannel = null;
		try(Session session = sessionFactory.openSession())
		{
			dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			return dbChannel;
		}
	}
	
	public List<CountingChannel> getTopFive() {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("unchecked")
			List<CountingChannel> channels = (List<CountingChannel>) session.createQuery("FROM CountingChannel"
					+ " ORDER BY CountPosition ASC")
					.setMaxResults(5).list();
			return channels;
		}	
	}
	
	public void saveCountConfig(CountingChannel countChannel) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();			
			session.saveOrUpdate(countChannel);
			session.getTransaction().commit();
		}
	}
	
	public void stopCount(MessageChannel channel) {
		String channelID = channel.getId();
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM CountingChannel WHERE ChannelID = :channelID")
				.setParameter("channelID", channelID)
				.executeUpdate();
			session.getTransaction().commit();
		}
		channel.sendMessage("**This count has been ended. If you wish to start again, you will have to start from 0!**").queue();
	}
	
	public boolean isDifferentUser(String userID, String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			return !dbChannel.getLastUser().equals(userID);
		}	
	}
	
	public void setCount(CountingChannel channel, int currentCount, String channelID, String lastUserID, String lastMessageId, String mode) {
		
		int newCount = 0;
		if (mode == "increment") newCount = currentCount + 1;
		else if (mode == "decrement") newCount = currentCount - 1;

        try(Session session = sessionFactory.openSession())
		{
			session.beginTransaction();
			
			channel.setChannelCount(newCount);
			channel.setLastUser(lastUserID);
			channel.setLastCountMessageId(lastMessageId);
			
			session.saveOrUpdate(channel);
			session.getTransaction().commit();			
		}	
	}
	
	public void resetCount(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			session.beginTransaction();
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			dbChannel.setChannelCount(0);
			dbChannel.setLastUser("");
			dbChannel.setReached69(false);
			dbChannel.resetLives();
			
			session.saveOrUpdate(dbChannel);
			session.getTransaction().commit();			
		}	
	}
	
	public void sendFailureMessage(Message message) {
		MessageEmbed failureEmbed = new EmbedBuilder()
				.setColor(Color.RED)
				.setTitle("You failed! :no_entry:")
				.setDescription("**You failed! The counter has been reset to 0!**")
				.build();
		message.addReaction(INCORRECT).queue();
		message.getChannel().sendMessageEmbeds(failureEmbed).queue();
	}

	public void sendWarning(Message message, Color colour, String warning) {
		MessageEmbed warningEmbed = new EmbedBuilder()
				.setColor(colour)
				.setTitle("Wait a minute! :warning:")
				.setDescription("**" + warning + "**")
				.build();
		message.addReaction(INCORRECT).queue();
		message.getChannel().sendMessageEmbeds(warningEmbed).queue();
	}
}
