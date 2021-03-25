package main.java.de.voidtech.gerald.routines.fun;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.CountingChannel;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class CountRoutine extends AbstractRoutine {
	private final static String CORRECT = "U+2705";
	private final static String INCORRECT = "U+274E";
	
	@Autowired
	private SessionFactory sessionFactory;
	
	//I did not make this function: 
	//https://stackoverflow.com/questions/237159/whats-the-best-way-to-check-if-a-string-represents-an-integer-in-java
	//TODO REVIEW: Code duplication in WelcomerCommand. Make a Utils class for this. This can be a static method in the Utils class. Maybe consider using NumberUtils from apache
	private boolean isInteger(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
	
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
			
			session.saveOrUpdate(dbChannel);
			session.getTransaction().commit();			
		}	
	}
	
	@Override
	public void executeInternal(Message message) {		
		if (isCountingChannel(message.getChannel().getId())) {
			if (isInteger(message.getContentRaw())) {
				if (isDifferentUser(message.getMember().getId(), message.getChannel().getId())) {
					int currentCount = getCount(message.getChannel().getId());
					int countGiven = Integer.parseInt(message.getContentRaw());
					
					if (countGiven == currentCount + 1) {
						setCount(currentCount, message.getChannel().getId(), message.getMember().getId(), "increment");
						message.addReaction(CORRECT).queue();
					} else if (countGiven == currentCount - 1) {
						setCount(currentCount, message.getChannel().getId(), message.getMember().getId(), "decrement");
						message.addReaction(CORRECT).queue();
					}else {
						resetCount(message.getChannel().getId());
						message.addReaction(INCORRECT).queue();
						message.getChannel().sendMessage("**You failed! The counter has been reset!**").queue();
					}		
				} else {
					message.getChannel().sendMessage("**You cannot count twice in a row! The counter has not been reset.**").queue();
					message.addReaction(INCORRECT).queue();
				}
			}			
		}		
	}

	@Override
	public String getDescription() {
		return "Handles counting channels";
	}

}