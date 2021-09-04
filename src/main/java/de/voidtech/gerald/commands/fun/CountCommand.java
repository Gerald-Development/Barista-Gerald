package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.CountingChannel;
import main.java.de.voidtech.gerald.service.CountingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.time.Instant;
import java.util.List;

@Command
public class CountCommand extends AbstractCommand {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private CountingService countService;
	
	private String formatAsMarkdown(String input) {
		return "```\n" + input + "\n```";
	}
	
	private void sendCountStatistics(MessageChannel channel) {
		try(Session session = sessionFactory.openSession())
		{
			CountingChannel dbChannel = countService.getCountingChannel(channel.getId());
						
			String current = formatAsMarkdown(String.valueOf(dbChannel.getChannelCount()));
			String lastUser = formatAsMarkdown(dbChannel.getLastUser().equals("") ? "Nobody" : channel.getJDA().getUserById(dbChannel.getLastUser()).getAsTag());
			String next = formatAsMarkdown(dbChannel.getChannelCount() - 1 + " or " + (dbChannel.getChannelCount() + 1));
			String reached69 = formatAsMarkdown(String.valueOf(dbChannel.hasReached69()));
			String numberOf69 = formatAsMarkdown(String.valueOf(dbChannel.get69ReachedCount()));
			String livesRemaining = formatAsMarkdown(String.valueOf(dbChannel.getLives()));
			
			MessageEmbed countStatsEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("Counting Statistics")
					.addField("Current Count", current, true)
					.addField("Next Count", next, true)
					.addField("Last User", lastUser, false)
					.addField("Has reached 69?", reached69, true)
					.addField("No. of times 69 has been reached", numberOf69, true)
					.addField("Lives Remaining", livesRemaining, true)
					.build();
			channel.sendMessageEmbeds(countStatsEmbed).queue();
		}	
	}
	
	private void startCountMethod(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) != null)
				context.getChannel().sendMessage("**There is already a count set up here!**").queue();
			else {
				CountingChannel newCountChannel = new CountingChannel(context.getChannel().getId(),	context.getGuild().getId());
				countService.saveCountConfig(newCountChannel);	
				context.getChannel().sendMessage("**The count has started! Send 1 to begin the game!**").queue();
			}
		} else context.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
	}
	
	private void stopCountMethod(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) != null) countService.stopCount(context.getChannel());
			else context.getChannel().sendMessage("**There is not a count set up here!**").queue();
		} else context.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
	}
	
	private void countStatsMethod(CommandContext context) {
		if (countService.getCountingChannel(context.getChannel().getId()) != null) {
			sendCountStatistics(context.getChannel());
		} else context.getChannel().sendMessage("**You need to use this command in a counting channel!**").queue();
	}
	
	private void countLeaderboardMethod(CommandContext context) {
		List<CountingChannel> topFiveChannels = countService.getTopFive();
		
		String leaderboard = "```js\n";
		
		int pos = 0;
		for (Object channel : topFiveChannels) {
			pos++;
			String channelID = ((CountingChannel) channel).getCountingChannel();
			String serverID = ((CountingChannel) channel).getServerID();
			int count = ((CountingChannel) channel).getChannelCount();
			
			leaderboard = "\n" + pos + ") Channel: " + context.getJDA().getGuildById(serverID).getName() + " > "
			+ context.getJDA().getGuildChannelById(channelID).getName() + "\n"
					+ "Count: " + count + "\n" + leaderboard;	
		}
		leaderboard += "```";
		
		MessageEmbed leaderboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("The 5 Highest Counts")
				.setDescription(leaderboard)
				.setTimestamp(Instant.now())
				.build();
		context.getChannel().sendMessageEmbeds(leaderboardEmbed).queue();
		
	}
	
	private void disableChat(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) == null)
				context.getChannel().sendMessage("**There is no count set up here!**").queue();
			else {
				CountingChannel channel = countService.getCountingChannel(context.getChannel().getId());
				channel.setIsTalkingAllowed(false);
				countService.saveCountConfig(channel);
				context.getChannel().sendMessage("**Non-counting messages sent in this channel will now be deleted!**").queue();
			}
		} else context.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
	}
	
	private void enableChat(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) == null)
				context.getChannel().sendMessage("**There is no count set up here!**").queue();
			else {
				CountingChannel channel = countService.getCountingChannel(context.getChannel().getId());
				channel.setIsTalkingAllowed(true);
				countService.saveCountConfig(channel);
				context.getChannel().sendMessage("**Non-counting messages sent in this channel will no longer be deleted!**").queue();
			}
		} else context.getChannel().sendMessage("**You need Manage Channels permissions to do that!**").queue();
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		
		switch(args.get(0)) {
		case "start":
			startCountMethod(context);
			break;
			
		case "stop":
			stopCountMethod(context);
			break;
		
		case "stats":
			countStatsMethod(context);
			break;
		
		case "enablechat":
			enableChat(context);
			break;
			
		case "disablechat":
			disableChat(context);
			break;
		
		case "leaderboard":
			countLeaderboardMethod(context);
			break;
			
		default:
			context.getChannel().sendMessage("**You need to use a valid subcommand!**\n" + this.getUsage()).queue();
			break;
				
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to create a designated Counting channel in your server!"
				+ " Each user must in turn count up starting from 0, if someone gets"
				+ " the count wrong, the counter resets from 0! Additionally, users"
				+ " may battle between eachother trying to either raise the count as"
				+ " high as possible, or get it as far below zero as possible by counting down.";
	}

	@Override
	public String getUsage() {
		return "count start\n"
				+ "count stop\n"
				+ "count stats\n"
				+ "count disablechat\n"
				+ "count enablechat";
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
		return new String[]{"counting"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}