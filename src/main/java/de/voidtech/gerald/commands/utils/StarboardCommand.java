package main.java.de.voidtech.gerald.commands.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.StarboardConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.StarboardService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@Command
public class StarboardCommand extends AbstractCommand {
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private StarboardService starboardService;
	
	private void setupStarboard(Message message, List<String> args, Server server) {
		if (starboardService.getStarboardConfig(server.getId()) != null)
			message.getChannel().sendMessage("**A Starboard has already been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
		else if (args.size() < 3)
				message.getChannel().sendMessage("**You need more arguments than that!**\n\n" + this.getUsage()).queue();
		else {
			String channelID = ParsingUtils.filterSnowflake(args.get(1));
			if (!ParsingUtils.isSnowflake(channelID))
				message.getChannel().sendMessage("**The channel you provided is not valid!**").queue();
			else if (!ParsingUtils.isInteger(args.get(2)))
				message.getChannel().sendMessage("**You need to specify a number for the star count!**").queue();
			else
				starboardService.completeStarboardSetup(message, channelID, args.get(2), server);
		}
	}
	
	private void disableStarboard(Message message, Server server) {
		if (starboardService.getStarboardConfig(server.getId()) != null)
			starboardService.deleteStarboardConfig(message, server);
		else
			message.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();	
	}

	private void changeChannel(Message message, List<String> args, Server server) {
		if (starboardService.getStarboardConfig(server.getId()) != null) {
			String channelID = ParsingUtils.filterSnowflake(args.get(1));
			if (!ParsingUtils.isSnowflake(channelID)) message.getChannel().sendMessage("**The channel you provided is not valid!**").queue();
			else {
				StarboardConfig config = starboardService.getStarboardConfig(server.getId());
				config.setChannelID(channelID);
				starboardService.updateConfig(config);
			}
		} else message.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();	
	}

	private void changeRequiredStarCount(Message message, List<String> args, Server server) {
		if (starboardService.getStarboardConfig(server.getId()) != null) {
			if (!ParsingUtils.isInteger(args.get(1))) message.getChannel().sendMessage("**You need to specify a number for the star count!**").queue();
			else {
				StarboardConfig config = starboardService.getStarboardConfig(server.getId());
				config.setRequiredStarCount(Integer.parseInt(args.get(1)));
				starboardService.updateConfig(config);
			}
		} else message.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();	
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (message.getMember().getPermissions().contains(Permission.MANAGE_CHANNEL)) {
			Server server = serverService.getServer(message.getGuild().getId());
			
			switch (args.get(0)) {
			case "setup":
				setupStarboard(message, args, server);
				break;
			case "count":
				changeRequiredStarCount(message, args, server);
				break;
			case "channel":
				changeChannel(message, args, server);
				break;
			case "disable":
				disableStarboard(message, server);
				break;
			default:
				message.getChannel().sendMessage("**That's not a valid subcommand! Try this instead:**\n\n" + this.getUsage()).queue();
			}
		} else message.getChannel().sendMessage("**You need the ** `Manage Channels` **Permission to do that!**").queue();	
	}

	@Override
	public String getDescription() {
		return "Do you like quoting things? Funny, interesting and more? Perfect!\n"
				+ "Our starboard system allows you to react to messages with the :star: emote and have them automatically sent to"
				+ " a starboard channel in your server! Your server admins can choose the channel and number of stars needed to get it pinned!";
	}

	@Override
	public String getUsage() {
		return "starboard setup [Channel mention / ID] [Required star count]\n"
				+ "starboard count [New number of stars needed]\n"
				+ "starboard channel [New channel mention / ID]\n"
				+ "starboard disable\n\n"
				+ "NOTE: You MUST run the setup command first!";
	}

	@Override
	public String getName() {
		return "starboard";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILS;
	}

	@Override
	public boolean isDMCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}

	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"autoquote", "quotechannel", "sb"};
		return aliases;
	}

}
