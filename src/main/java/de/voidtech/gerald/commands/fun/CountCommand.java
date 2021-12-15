package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.CountingChannel;
import main.java.de.voidtech.gerald.service.CountingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class CountCommand extends AbstractCommand {
	
	@Autowired
	private CountingService countService;
	
	private void sendCountStatistics(CommandContext context) {
			CountingChannel dbChannel = countService.getCountingChannel(context.getChannel().getId());
			context.reply(countService.getCountStatsEmbedForChannel(dbChannel, context.getJDA()));	
	}
	
	private void startCountMethod(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) != null)
				context.reply("**There is already a count set up here!**");
			else {
				CountingChannel newCountChannel = new CountingChannel(context.getChannel().getId(),	context.getGuild().getId());
				countService.saveCountConfig(newCountChannel);	
				context.reply("**The count has started! Send 1 to begin the game!**");
			}
		} else context.reply("**You need Manage Channels permissions to do that!**");
	}
	
	private void stopCountMethod(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) != null) countService.stopCount(context.getChannel());
			else context.reply("**There is not a count set up here!**");
		} else context.reply("**You need Manage Channels permissions to do that!**");
	}
	
	private void countStatsMethod(CommandContext context) {
		if (countService.getCountingChannel(context.getChannel().getId()) != null) {
			sendCountStatistics(context);
		} else context.reply("**You need to use this command in a counting channel!**");
	}
	
	private void countLeaderboardMethod(CommandContext context) {
		List<CountingChannel> topFiveChannels = countService.getTopFive();
		
		String leaderboard;
		
		int pos = 0;
		StringBuilder leaderboardBuilder = new StringBuilder("```js\n");
		for (Object channel : topFiveChannels) {
			pos++;
			String channelID = ((CountingChannel) channel).getCountingChannel();
			String serverID = ((CountingChannel) channel).getServerID();
			int count = ((CountingChannel) channel).getChannelCount();
			
			leaderboardBuilder.insert(0, "\n" + pos + ") Channel: " + context.getJDA().getGuildById(serverID).getName() + " > "
					+ context.getJDA().getGuildChannelById(channelID).getName() + "\n"
					+ "Count: " + count + "\n");
		}
		leaderboard = leaderboardBuilder.toString();
		leaderboard += "```";
		
		MessageEmbed leaderboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("The 5 Highest Counts")
				.setDescription(leaderboard)
				.setTimestamp(Instant.now())
				.build();
		context.reply(leaderboardEmbed);
		
	}
	
	private void disableChat(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) == null)
				context.reply("**There is no count set up here!**");
			else {
				CountingChannel channel = countService.getCountingChannel(context.getChannel().getId());
				channel.setIsTalkingAllowed(false);
				countService.saveCountConfig(channel);
				context.reply("**Non-counting messages sent in this channel will now be deleted!**");
			}
		} else context.reply("**You need Manage Channels permissions to do that!**");
	}
	
	private void enableChat(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (countService.getCountingChannel(context.getChannel().getId()) == null)
				context.reply("**There is no count set up here!**");
			else {
				CountingChannel channel = countService.getCountingChannel(context.getChannel().getId());
				channel.setIsTalkingAllowed(true);
				countService.saveCountConfig(channel);
				context.reply("**Non-counting messages sent in this channel will no longer be deleted!**");
			}
		} else context.reply("**You need Manage Channels permissions to do that!**");
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
			context.reply("**You need to use a valid subcommand!**\n" + this.getUsage());
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
	
	@Override
	public boolean isSlashCompatible() {
		return true;
	}
}