package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

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
						showAllLevelRoles(context, server, context.getGuild());
						break;
					case "addrole":
						addLevelUpRole(context, args, server);
						break;
					case "removerole":
						removeLevelUpRole(context, args, server);
						break;
					case "leaderboard":
						showServerLeaderboard(context, server);
						break;
					case "lb":
						showServerLeaderboard(context, server);
						break;
				}
			}
		}
	}

	private void showServerLeaderboard(CommandContext context, Server server) {
		List<Experience> topTenMembers = xpService.getServerLeaderboardChunk(server.getId(), 10, 0);
		int userPosition = xpService.getUserLeaderboardPosition(server.getId(), context.getAuthor().getId());
		Experience userXP = xpService.getUserExperience(context.getAuthor().getId(), server.getId());
		
		String leaderboard = "";
		int rank = 1;
		
		for (Experience xp : topTenMembers) {
			leaderboard += numberToEmoji(rank) + " <@" + xp.getUserID() + ">\n";
			leaderboard += "```js\nLevel " + xp.getCurrentLevel() + " | XP " + xp.getTotalExperience() + "\n```";
			rank++;
		}
		
		leaderboard += "\n**Your Position**\n";
		leaderboard += numberToEmoji(userPosition) + " <@" + userXP.getUserID() + ">\n";
		leaderboard += "```js\nLevel " + userXP.getCurrentLevel() + " | XP " + userXP.getTotalExperience() + "\n```";
		
		MessageEmbed leaderboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setDescription(leaderboard)
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
		if (guild.getRoleById(roleID) == null) {
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
		String finalNumber = "";
		for (String digit : digits) {
			finalNumber += convertSingleDigitToEmoji(digit);
		}
		return finalNumber;
	}
	
	private String convertSingleDigitToEmoji(String digit) {
		switch (digit) {
		case "0":
			return ":zero";
		case "1":
			return ":one:";
		case "2":
			return ":two:";
		case "3":
			return ":three:";
		case "4":
			return ":four:";
		case "5":
			return ":five:";
		case "6":
			return ":six:";
		case "7":
			return ":seven:";
		case "8":
			return ":eight:";
		case "9":
			return ":nine:";
		case "10":
			return ":ten:";
		default:
			return ":zero:";
	}
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

	private void showAllLevelRoles(CommandContext context, Server server, Guild guild) {
		List<LevelUpRole> levelUpRoles = xpService.getAllLevelUpRolesForServer(server.getId());
		if (levelUpRoles.isEmpty()) 
			context.reply("**There are no level roles set up in this server! See the help page for more info!**");
		else {
			String roleMessage = "";
			for (LevelUpRole role : levelUpRoles) {
				roleMessage += "`" + role.getLevel() + "` - " + "<@&" + role.getRoleID() + ">\n";
			}
			MessageEmbed levelRolesEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("Level up roles for " + guild.getName())
					.setDescription(roleMessage)
					.build();
			context.reply(levelRolesEmbed);
		}
	}

	private void sendLevelCard(CommandContext context, Member member, long serverID) {
		Experience userXP = xpService.getUserExperience(member.getId(), serverID);
		
		byte[] xpCard = xpService.getExperienceCard(member.getUser().getAvatarUrl(),
				userXP.getCurrentExperience(), xpService.xpNeededForLevel(userXP.getNextLevel()),
				userXP.getCurrentLevel(), 1, member.getUser().getName() + "#" + member.getUser().getDiscriminator(),
				"#FF0000", "#2E2E2E");
		context.replyWithFile(xpCard, "xpcard.png");
	}

	@Override
	public String getDescription() {
		return "You know the drill, the more messages you send, the more experience you gain!\n"
				+ "You can gain up to 15 experience per minute.\n"
				+ "Server admins can configure roles that are given to you when you reach a certain level.\n"
				+ "To stop people from checking their XP, you can disable the XP command.\n"
				+ "If you want to stop people from gaining XP, disable the r-xp routine.";
	}

	@Override
	public String getUsage() {
		return "xp\n"
				+ "xp levels\n"
				+ "xp addrole [level] [role]\n"
				+ "xp removerole [level]\n"
				+ "xp leaderboard";
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
