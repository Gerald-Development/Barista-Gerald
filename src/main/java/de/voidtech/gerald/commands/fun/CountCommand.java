package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.CountingChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

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
	
	private List<CountingChannel> getTopFive() {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("unchecked")
			List<CountingChannel> channels = (List<CountingChannel>) session.createQuery("FROM CountingChannel"
					+ " ORDER BY CountPosition ASC")
					.setMaxResults(5).list();
			return channels;
		}	
	}
	
	private void startCount(Message message) {
		String channelID = message.getChannel().getId();
		
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			CountingChannel newCountChannel = new CountingChannel("", "", 0, "", false, 0);
			
			newCountChannel.setCountingChannel(channelID);
			newCountChannel.setServerID(message.getGuild().getId());
			newCountChannel.setChannelCount(0);
			newCountChannel.setLastUser(message.getJDA().getSelfUser().getId());
			newCountChannel.setReached69(false);
			newCountChannel.setNumberOfTimes69HasBeenReached(0);
			
			session.saveOrUpdate(newCountChannel);
			session.getTransaction().commit();
		}
		message.getChannel().sendMessage("**The count has started! Send 1 to begin the game!**").queue();
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
	
	private String formatAsMarkdown(String input) {
		return "```\n" + input + "\n```";
	}
	
	private void sendCountStatistics(MessageChannel channel) {
		String channelID = channel.getId();
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = (CountingChannel) session.createQuery("FROM CountingChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
						
			String current = formatAsMarkdown(String.valueOf(dbChannel.getChannelCount()));
			String lastUser = formatAsMarkdown(channel.getJDA().getUserById(dbChannel.getLastUser()).getAsTag());
			String next = formatAsMarkdown(String.valueOf(dbChannel.getChannelCount() - 1) + " or " + String.valueOf(dbChannel.getChannelCount() + 1));
			String reached69 = formatAsMarkdown(String.valueOf(dbChannel.hasReached69()));
			String numberOf69 = formatAsMarkdown(String.valueOf(dbChannel.get69ReachedCount()));
			
			MessageEmbed countStatsEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("Counting Statistics")
					.addField("Current Count", current, true)
					.addField("Next Count", next, true)
					.addField("Last User", lastUser, false)
					.addField("Has reached 69?", reached69, true)
					.addField("No. of times 69 has been reached", numberOf69, true)
					.build();
			channel.sendMessage(countStatsEmbed).queue();
		}	
	}
	
	private void startCountMethod(Message message) {
		if (message.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countingChannelExists(message.getChannel().getId())) {
				message.getChannel().sendMessage("**There is already a count set up here!**").queue();
			} else {
				startCount(message);	
			}
		} else {
			message.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
		}	
	}
	
	private void stopCountMethod(Message message) {
		if (message.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countingChannelExists(message.getChannel().getId())) {
				stopCount(message.getChannel());
			} else {
				message.getChannel().sendMessage("**There is not a count set up here!**").queue();
			}
		} else {
			message.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
		}
	}
	
	private void countStatsMethod(Message message) {
		if (countingChannelExists(message.getChannel().getId())) {
			sendCountStatistics(message.getChannel());
		} else {
			message.getChannel().sendMessage("**You need to use this command in a counting channel!**").queue();
		}
	}
	
	private void countLeaderboardMethod(Message message) {
		List<CountingChannel> topFiveChannels = getTopFive();
		
		String leaderboard = "```js\n";
		
		int pos = 0;
		for (Object channel : topFiveChannels) {
			pos++;
			String channelID = ((CountingChannel) channel).getCountingChannel();
			String serverID = ((CountingChannel) channel).getServerID();
			int count = ((CountingChannel) channel).getChannelCount();
			
			leaderboard += "\n" + pos + ") Channel: " + message.getJDA().getGuildById(serverID).getName() + " > "
			+ message.getJDA().getGuildChannelById(channelID).getName() + "\n"
					+ "Count: " + count + "\n";
			
		}
		leaderboard += "```";
		
		MessageEmbed leaderboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("The 5 Highest Counts")
				.setDescription(leaderboard)
				.setTimestamp(Instant.now())
				.build();
		message.getChannel().sendMessage(leaderboardEmbed).queue();
		
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		
		switch(args.get(0)) {
		case "start":
			startCountMethod(message);
			break;
			
		case "stop":
			stopCountMethod(message);
			break;
		
		case "stats":
			countStatsMethod(message);
			break;
		
		case "leaderboard":
			countLeaderboardMethod(message);
			break;
			
		default:
			message.getChannel().sendMessage("**You need to use a valid subcommand!**\n" + this.getUsage()).queue();
			break;
				
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to create a designated Counting channel in your server! Each user must in turn count up starting from 0, if someone gets the count wrong, the counter resets! Additionally, users may battle between eachother trying to either raise the count as high as possible, or get it as far below zero as possible by counting down.";
	}

	@Override
	public String getUsage() {
		return "count start\n"
				+ "count stop\n"
				+ "count stats\n";
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
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"counting"};
		return aliases;
	}
}