package main.java.de.voidtech.gerald.service;

import java.awt.Color;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.CountingChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class CountingService {
	
	private final static String INCORRECT = "U+274C";

	@Autowired
	public SessionFactory sessionFactory;	
	
	public CountingChannel getCountingChannel (String channelID) {
		CountingChannel dbChannel;
		try(Session session = sessionFactory.openSession())
		{
			dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			return dbChannel;
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
		if (mode.equals("increment")) newCount = currentCount + 1;
		else if (mode.equals("decrement")) newCount = currentCount - 1;

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
	
	private String formatAsMarkdown(String input) {
		return "```\n" + input + "\n```";
	}
	
	public MessageEmbed getCountStatsEmbedForChannel(CountingChannel dbChannel, JDA jda) {
		String current = formatAsMarkdown(String.valueOf(dbChannel.getChannelCount()));
		String lastUser = formatAsMarkdown(dbChannel.getLastUser().equals("") ? "Nobody" : Objects.requireNonNull(jda.getUserById(dbChannel.getLastUser())).getAsTag());
		String next = formatAsMarkdown(dbChannel.getChannelCount() - 1 + " or " + (dbChannel.getChannelCount() + 1));
		String reached69 = formatAsMarkdown(String.valueOf(dbChannel.hasReached69()));
		String numberOf69 = formatAsMarkdown(String.valueOf(dbChannel.get69ReachedCount()));
		String livesRemaining = formatAsMarkdown(String.valueOf(dbChannel.getLives()));

		return new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Counting Statistics")
				.addField("Current Count", current, true)
				.addField("Next Count", next, true)
				.addField("Last User", lastUser, false)
				.addField("Has reached 69?", reached69, true)
				.addField("No. of times 69 has been reached", numberOf69, true)
				.addField("Lives Remaining", livesRemaining, true)
				.build();
	}	
}