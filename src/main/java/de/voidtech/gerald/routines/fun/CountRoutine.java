package main.java.de.voidtech.gerald.routines.fun;

import java.awt.Color;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.CountingChannel;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Routine
public class CountRoutine extends AbstractRoutine {
	private final static String CORRECT = "U+2705";
	private final static String INCORRECT = "U+274E";
	private final static String LETTER_N = "U+1F1F3";
	private final static String LETTER_I = "U+1F1EE";
	private final static String LETTER_C = "U+1F1E8";
	private final static String LETTER_E = "U+1F1EA";
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private boolean isCountingChannel(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			return dbChannel != null;
		}		
	}
	
	private boolean isDifferentUser(String userID, String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			return !dbChannel.getLastUser().equals(userID);
		}	
	}
	
	private boolean hasChannelReached69(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			return dbChannel.hasReached69();
		}	
	}
	
	private int getCount(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			return dbChannel.getChannelCount();
		}		
	}
	
	private void setCount(int currentCount, String channelID, String lastUserID, String mode) {
		int newCount = 0;
		if (mode == "increment") {
			newCount = currentCount + 1;
		} else if (mode == "decrement") {
			newCount = currentCount - 1;
		}
		
		try(Session session = sessionFactory.openSession())
		{
			session.beginTransaction();
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			dbChannel.setChannelCount(newCount);
			dbChannel.setLastUser(lastUserID);
			
			session.saveOrUpdate(dbChannel);
			session.getTransaction().commit();			
		}	
	}
	
	private void resetCount(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			session.beginTransaction();
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			dbChannel.setChannelCount(0);
			dbChannel.setLastUser("");
			dbChannel.setReached69(false);
			
			session.saveOrUpdate(dbChannel);
			session.getTransaction().commit();			
		}	
	}	

	private boolean shouldSendNice(int countGiven, String channelID) {
		return ((countGiven == 69 || countGiven == -69) && !hasChannelReached69(channelID));
	}
	
	private void update69ReachedStatus(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			session.beginTransaction();
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			dbChannel.setReached69(true);
			dbChannel.setNumberOfTimes69HasBeenReached(dbChannel.get69ReachedCount() + 1);
			
			session.saveOrUpdate(dbChannel);
			session.getTransaction().commit();			
		}	
	}
	
	private void sendNice(Message message) {
		message.addReaction(LETTER_N).queue();
		message.addReaction(LETTER_I).queue();
		message.addReaction(LETTER_C).queue();
		message.addReaction(LETTER_E).queue();
		
		update69ReachedStatus(message.getChannel().getId());
	}

	private void sendFailureMessage(Message message) {
		MessageEmbed failureEmbed = new EmbedBuilder()
				.setColor(Color.RED)
				.setTitle("You failed! :no_entry:")
				.setDescription("**You failed! The counter has been reset!**")
				.build();
		message.addReaction(INCORRECT).queue();
		message.getChannel().sendMessageEmbeds(failureEmbed).queue();
	}

	private void sendWarning(Message message, String warning) {
		MessageEmbed warningEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Wait a minute! :warning:")
				.setDescription("**" + warning + "**")
				.build();
		message.addReaction(INCORRECT).queue();
		message.getChannel().sendMessageEmbeds(warningEmbed).queue();
	}
	
	@Override
	public void executeInternal(Message message) {		
		if (isCountingChannel(message.getChannel().getId())) {
			if (ParsingUtils.isInteger(message.getContentRaw())) {
				if (isDifferentUser(message.getMember().getId(), message.getChannel().getId())) {
					int currentCount = getCount(message.getChannel().getId());
					int countGiven = Integer.parseInt(message.getContentRaw());
					
					if (countGiven == currentCount + 1) {
						setCount(currentCount, message.getChannel().getId(), message.getMember().getId(), "increment");
						message.addReaction(CORRECT).queue();
						if (shouldSendNice(countGiven, message.getChannel().getId())) {
							sendNice(message);
						}
					} else if (countGiven == currentCount - 1) {
						setCount(currentCount, message.getChannel().getId(), message.getMember().getId(), "decrement");
						message.addReaction(CORRECT).queue();
						if (shouldSendNice(countGiven, message.getChannel().getId())) {
							sendNice(message);
						}
					} else {
						resetCount(message.getChannel().getId());
						sendFailureMessage(message);
					}
				} else {
					sendWarning(message, "You cannot count twice in a row! The counter has not been reset.");
				}
			}			
		}		
	}

	@Override
	public String getDescription() {
		return "Allows channels with counting enabled to work";
	}
	
	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	@Override
	public String getName() {
		return "r-count";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.FUN;
	}

}