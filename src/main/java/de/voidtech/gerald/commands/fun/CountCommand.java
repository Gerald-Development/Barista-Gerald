package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.CountingChannel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

@Command
public class CountCommand extends AbstractCommand {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private boolean countingChannelExists (String channelID) {
		CountingChannel dbChannel = null;
		try(Session session = sessionFactory.openSession())
		{
			dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			return dbChannel != null;
		}
	}
	
	private void startCount(MessageChannel channel) {
		String channelID = channel.getId();
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			CountingChannel newCountChannel = new CountingChannel(channelID, 0, "");
			
			newCountChannel.setCountingChannel(channelID);
			newCountChannel.setChannelCount(0);
			newCountChannel.setLastUser("");
			
			session.saveOrUpdate(newCountChannel);
			session.getTransaction().commit();
		}
		channel.sendMessage("**The count has started! Send 1 to begin the game!**").queue();
	}
	
	private void stopCount(MessageChannel channel) {
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
	
	private void getCurrentCount(MessageChannel channel) {
		String channelID = channel.getId();
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			
			int current = dbChannel.getChannelCount();
			int next = dbChannel.getChannelCount() + 1;
			channel.sendMessage("The current count is **" + current + "**. Send **" + next + "** or the count will reset!").queue();
		}	
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		
		if (args.get(0).equals("start")) {
			if (message.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
				if (countingChannelExists(message.getChannel().getId())) {
					message.getChannel().sendMessage("**There is already a count set up here!**").queue();
				} else {
					startCount(message.getChannel());	
				}
			} else {
				message.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
			}			
			
		} else if (args.get(0).equals("stop")) {
			if (message.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
				if (countingChannelExists(message.getChannel().getId())) {
					stopCount(message.getChannel());
				} else {
					message.getChannel().sendMessage("**There is not a count set up here!**").queue();
				}
			} else {
				message.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
			}
			
		} else if (args.get(0).equals("current")) {
			getCurrentCount(message.getChannel());
		} else {
			message.getChannel().sendMessage("**You need to use a valid subcommand!**\n" + this.getUsage()).queue();
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to create a designated Counting channel in your server! Each user must in turn count up starting from 0, if someone gets the count wrong, the counter resets! Additionally, users may battle between eachother trying to either raise the count as high as possible, or get it as far below zero as possible by counting down. Original idea: https://top.gg/bot/769855226425245706";
	}

	@Override
	public String getUsage() {
		return "count [start/stop/current]";
	}

	@Override
	public String getName() {
		return "count";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
}