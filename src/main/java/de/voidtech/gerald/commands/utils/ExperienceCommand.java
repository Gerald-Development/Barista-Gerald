package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Experience;
import main.java.de.voidtech.gerald.entities.LevelUpRole;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ExperienceService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

@Command
public class ExperienceCommand extends AbstractCommand {

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private ExperienceService xpService;
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		Server server = serverService.getServer(context.getGuild().getId());
		if (args.isEmpty()) sendLevelCard(context, context.getMember(), server.getId());
		else {
			String ID = ParsingUtils.filterSnowflake(args.get(0));
			if (ParsingUtils.isSnowflake(ID)) {
				Member mentionedMember = context.getGuild().retrieveMemberById(ID).complete();
				if (mentionedMember == null) context.getChannel().sendMessage("**You need to mention another member to see their XP!**").queue();
				else sendLevelCard(context, mentionedMember, server.getId());	
			} else {
				switch (args.get(0)) {
					case "levels":
						showAllLevelRoles(context, server);
						break;
					case "addrole":
						addLevelUpRole(context, args, server);
						break;
					case "removerole":
						removeLevelUpRole(context, args, server);
						break;
					case "leaderboard":
					case "lb":
						showServerLeaderboard(context, server);
						break;
					case "togglemsg":
						toggleLevelUpMessages(context, server);
						break;
					case "noxp":
						handleNoXpChannelSettings(context, server);
						break;
					case "rate":
						handleXpRate(context, server);
						break;
					default:
						context.reply("**That's not a valid subcommand!**\n" + this.getUsage());
						break;
				}
			}
		}
	}

	private void handleXpRate(CommandContext context, Server server) {
		if (context.getArgs().size() == 1) {
			context.reply("**XP gain rate is currently `" + xpService.getServerExperienceRate(server.getId()) + "` per minute**");
		} else {
			if (!context.getMember().hasPermission(Permission.MANAGE_SERVER)) {
				context.reply("**You need the Manage Server Permission to do that!**");
				return;
			}

			if (context.getArgs().get(1).equals("random")) {
				xpService.setServerGainRate(-1, server.getId());
				context.reply("**XP gain rate is now randomised (between 1 and 15)**");
				return;
			}

			if (!ParsingUtils.isInteger(context.getArgs().get(1))) {
				context.reply("**The provided XP gain rate must be a number!**");
				return;
			}
			int gainRate = Integer.valueOf(context.getArgs().get(1));
			if (gainRate < 1 || gainRate > 25) {
				context.reply("**XP gain rate must be between 1 and 25**");
				return;
			}
			xpService.setServerGainRate(gainRate, server.getId());
			context.reply("**Set XP gain rate to `" + gainRate + "`**");
		}
	}

	private void showNoXpHelp(CommandContext context) {
		context.reply("**No XP Settings:**\n"
				+ "list - shows all channels where xp will not be gained\n"
				+ "add - add a channel that will not gain xp\n"
				+ "clear - remove all no xp channels\n"
				+ "remove - remove a channel that will not gain xp\n\n"
				+ "When adding or removing a channel from the no xp list, you will be prompted to enter a channel mention or ID.");
	}

	private void handleNoXpChannelSettings(CommandContext context, Server server) {
		String mode;
		if (context.getArgs().size() < 2) mode = "help";
		else mode = context.getArgs().get(1);
		
		switch (mode) {
			case "list":
				showNoXPChannels(context, server);
				break;
			case "add": 
				addNoXPChannel(context, server);
				break;
			case "remove":
				removeNoXPChannel(context, server);
				break;
			case "clear":
				clearNoXPChannels(context, server);
				break;
			case "help":
				showNoXpHelp(context);
				break;
			default:
				showNoXpHelp(context);
				break;
		}
	}

	private void clearNoXPChannels(CommandContext context, Server server) {
		if (!context.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
			context.reply("**You need the Manage Server Permission to do that!**");
			return;
		}
		xpService.clearNoXpChannels(server.getId());
		context.reply("**No XP Channels have been cleared!**");		
	}

	private void removeNoXPChannel(CommandContext context, Server server) {
		if (!context.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
			context.reply("**You need the Manage Server Permission to do that!**");
			return;
		}
		if (context.getArgs().size() < 3) {
			context.reply("**You need to specify a channel to remove!**");
			return;
		}
		String channelID = ParsingUtils.filterSnowflake(context.getArgs().get(2));
		TextChannel channel = context.getGuild().getTextChannelById(channelID);
		if (channel == null) {
			context.reply("**You need to specify a valid text channel!**");
			return;
		}
		xpService.deleteNoXpChannel(channelID, server.getId());
		context.reply("**No XP channel** <#" + channelID + "> **has been removed!**");
	}

	private void addNoXPChannel(CommandContext context, Server server) {
		if (!context.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
			context.reply("**You need the Manage Server Permission to do that!**");
			return;
		}
		if (context.getArgs().size() < 3) {
			context.reply("**You need to specify a channel to add!**");
			return;
		}
		String channelID = ParsingUtils.filterSnowflake(context.getArgs().get(2));
		TextChannel channel = context.getGuild().getTextChannelById(channelID);
		if (channel == null) {
			context.reply("**You need to specify a valid text channel!**");
			return;
		}
		xpService.addNoXpChannel(channelID, server.getId());
		context.reply("**No XP channel** <#" + channelID + "> **has been added!**");
	}

	private void showNoXPChannels(CommandContext context, Server server) {
		List<String> channels = xpService.getNoExperienceChannelsForServer(server.getId(), context.getJDA())
				.stream().map(channel -> "<#" + channel + ">").collect(Collectors.toList());
		
		String messageText = channels.isEmpty() ? "No channels to show!" : String.join("\n", channels);
		
		MessageEmbed noXPChannelsEmbed = new EmbedBuilder()
				.setDescription(messageText)
				.setColor(Color.ORANGE)
				.build();
		context.reply(noXPChannelsEmbed);
	}

	private void toggleLevelUpMessages(CommandContext context, Server server) {
		boolean userCanToggle = context.getMember().hasPermission(Permission.MANAGE_SERVER);
		if (!userCanToggle) context.reply("**You need the Manage Server permission to toggle level up messages!**");
		else {
			boolean enabled = xpService.toggleLevelUpMessages(server.getId());
			context.reply("**Level up messages are now " + (enabled ? "enabled**" : "disabled**"));
		}
	}

	private void showServerLeaderboard(CommandContext context, Server server) {
		List<Experience> topTenMembers = xpService.getServerLeaderboardChunk(server.getId(), 5, 0);
		int userPosition = xpService.getUserLeaderboardPosition(server.getId(), context.getAuthor().getId());
		Experience userXP = xpService.getUserExperience(context.getAuthor().getId(), server.getId());
		
		StringBuilder leaderboard = new StringBuilder();
		int rank = 1;
		
		for (Experience xp : topTenMembers) {
			leaderboard.append(numberToEmoji(rank)).append(" <@").append(xp.getUserID()).append(">\n");
			leaderboard.append("```js\nLevel ").append(xp.getCurrentLevel()).append(" | XP ").append(xp.getTotalExperience()).append("\n```");
			rank++;
		}
		
		leaderboard.append("\n**Your Position**\n");
		leaderboard.append(numberToEmoji(userPosition)).append(" <@").append(userXP.getUserID()).append(">\n");
		leaderboard.append("```js\nLevel ").append(userXP.getCurrentLevel()).append(" | XP ").append(userXP.getTotalExperience()).append("\n```");
		
		MessageEmbed leaderboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setDescription(leaderboard.toString())
				.setTitle(context.getGuild().getName() + "'s Most Active Members")
				.build();
		context.reply(leaderboardEmbed);
	}

	private void addLevelUpRole(CommandContext context, List<String> args, Server server) {
		if (args.size() < 3) {
			context.reply("**You need to supply more arguments! Make sure you have a level and a role mention or ID!**");
			return;
		}
		
		String level = args.get(1);
		if (!ParsingUtils.isInteger(level)) {
			context.reply("**You need to provide a valid number for the level!**");
			return;
		}
		
		if (xpService.serverHasRoleForLevel(server.getId(), Integer.parseInt(level))) {
			context.reply("**There is already a role set up for this level!**");
			return;
		}
		
		String roleID = ParsingUtils.filterSnowflake(args.get(2));
		if (!ParsingUtils.isSnowflake(roleID)) {
			context.reply("**Please provide a valid role mention or role ID**");
			return;
		}
		
		Guild guild = context.getJDA().getGuildById(server.getGuildID());
		if (Objects.requireNonNull(guild).getRoleById(roleID) == null) {
			context.reply("**Please provide a valid role mention or role ID**");
			return;
		}
		
		LevelUpRole role = new LevelUpRole(roleID, server.getId(), Integer.parseInt(level));
		xpService.saveLevelUpRole(role);
		MessageEmbed newRoleEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Added Level Up Role!")
				.setDescription("Level: `" + level + "`\nRole: <@&" + roleID + ">")
				.build();
		context.reply(newRoleEmbed);
	}
	
	private String numberToEmoji(int number) {
		List<String> digits = Arrays.asList(String.valueOf(number).split("")); //Wowzer
		StringBuilder finalNumber = new StringBuilder();
		for (String digit : digits) {
			finalNumber.append(ParsingUtils.convertSingleDigitToEmoji(digit));
		}
		return finalNumber.toString();
	}
	
	private void removeLevelUpRole(CommandContext context, List<String> args, Server server) {
		if (args.size() < 2) {
			context.reply("**You need to provide a level to remove the role from!**");
			return;
		}
		
		String level = args.get(1);
		if (!ParsingUtils.isInteger(level)) {
			context.reply("**You need to provide a valid number for the level!**");
			return;
		}
		
		if (!xpService.serverHasRoleForLevel(server.getId(), Integer.parseInt(level))) {
			context.reply("**There isn't a role set up for this level!**");
			return;
		}
		
		xpService.removeLevelUpRole(Integer.parseInt(level), server.getId());
		MessageEmbed roleDeletedEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Removed Level Up Role!")
				.setDescription("Role for level `" + level + "` has been removed")
				.build();
		context.reply(roleDeletedEmbed);
	}

	private void showAllLevelRoles(CommandContext context, Server server) {
		String rate = xpService.getServerExperienceRate(server.getId());
		List<LevelUpRole> levelUpRoles = xpService.getAllLevelUpRolesForServer(server.getId());
		if (levelUpRoles.isEmpty()) 
			context.reply("**There are no level roles set up in this server! See the help page for more info!**");
		else {
			StringBuilder roleMessage = new StringBuilder();
			for (LevelUpRole role : levelUpRoles) {
				roleMessage.append("`").append(role.getLevel()).append("` - ").append("<@&").append(role.getRoleID()).append(">\n");
			}
			MessageEmbed levelRolesEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("Level up roles for " + context.getGuild().getName())
					.setDescription(roleMessage.toString())
					.setFooter("XP Gain Rate: " + rate + " per minute")
					.build();
			context.reply(levelRolesEmbed);
		}
	}

	private void sendLevelCard(CommandContext context, Member member, long serverID) {
		Experience userXP = xpService.getUserExperience(member.getId(), serverID);
		
		if (userXP == null) {
			context.reply("**User is not ranked yet!**");
			return;
		}
		
		String avatarURL = member.getUser().getAvatarUrl();
		long currentExperience = xpService.xpGainedToNextLevelWithoutPreviousLevels(userXP.getNextLevel(), userXP.getTotalExperience());
		long xpNeeded = xpService.xpNeededForLevelWithoutPreviousLevels(userXP.getNextLevel());
		long level = userXP.getCurrentLevel();
		long rank = xpService.getUserLeaderboardPosition(serverID, userXP.getUserID());
		String username = member.getUser().getName();
		String discriminator = member.getUser().getDiscriminator(); 
		
		byte[] xpCard = xpService.getExperienceCard(avatarURL, currentExperience, xpNeeded, level, rank, username, discriminator);
		context.replyWithFile(xpCard, "xpcard.png");
	}

	@Override
	public String getDescription() {
		return "You know the drill, the more messages you send, the more experience you gain!\n"
				+ "You can gain up to 15 experience per minute.\n"
				+ "Server admins can configure roles that are given to you when you reach a certain level.\n"
				+ "To stop people from checking their XP, you can disable the XP command.\n"
				+ "If you want to stop people from gaining XP, disable the r-xp routine.\n"
				+ "To disable the level up messages, use the togglemsg subcommand.\n"
				+ "To control which channels will not allow members to gain XP, use 'xp noxp help' to see how to set it up!";
	}

	@Override
	public String getUsage() {
		return "xp\n"
				+ "xp levels\n"
				+ "xp addrole [level] [role]\n"
				+ "xp removerole [level]\n"
				+ "xp togglemsg\n"
				+ "xp leaderboard\n"
				+ "xp noxp [help/list/add/remove/clear]\n" +
				"xp rate [random/(choose an xp rate)]";
	}

	@Override
	public String getName() {
		return "xp";
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
		return false;
	}

	@Override
	public String[] getCommandAliases() {
		return new String[] {"level", "rank"};
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

	@Override
	public boolean isSlashCompatible() {
		return false;
	}

}
