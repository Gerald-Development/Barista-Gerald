package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
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
import net.dv8tion.jda.api.entities.MessageChannel;
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
		if (args.isEmpty()) sendLevelCard(context.getChannel(), context.getMember(), server.getId());
		else {
			String ID = ParsingUtils.filterSnowflake(args.get(0));
			if (ParsingUtils.isSnowflake(ID)) {
				Member mentionedMember = context.getGuild().getMemberById(ID);
				if (mentionedMember == null) context.getChannel().sendMessage("**You need to mention another member to see their XP!**").queue();
				else sendLevelCard(context.getChannel(), mentionedMember, server.getId());	
			} else {
				switch (args.get(0)) {
					case "levels":
						showAllLevelRoles(context.getChannel(), server, context.getGuild());
						break;
					case "addrole":
						addLevelUpRole(context.getChannel(), args, server);
						break;
					case "removerole":
						removeLevelUpRole(context.getChannel(), args, server);
						break;
				}
			}
		}
	}

	private void addLevelUpRole(MessageChannel channel, List<String> args, Server server) {
		if (args.size() < 3) {
			channel.sendMessage("**You need to supply more arguments! Make sure you have a level and a role mention or ID!**").queue();
			return;
		}
		
		String level = args.get(1);
		if (!ParsingUtils.isInteger(level)) {
			channel.sendMessage("**You need to provide a valid number for the level!**").queue();
			return;
		}
		
		if (xpService.serverHasRoleForLevel(server.getId(), Integer.parseInt(level))) {
			channel.sendMessage("**There is already a role set up for this level!**").queue();
			return;
		}
		
		String roleID = ParsingUtils.filterSnowflake(args.get(2));
		if (!ParsingUtils.isSnowflake(roleID)) {
			channel.sendMessage("**Please provide a valid role mention or role ID**").queue();
			return;
		}
		
		Guild guild = channel.getJDA().getGuildById(server.getGuildID());
		if (guild.getRoleById(roleID) == null) {
			channel.sendMessage("**Please provide a valid role mention or role ID**").queue();
			return;
		}
		
		LevelUpRole role = new LevelUpRole(roleID, server.getId(), Integer.parseInt(level));
		xpService.saveLevelUpRole(role);
		MessageEmbed newRoleEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Added Level Up Role!")
				.setDescription("Level: `" + level + "`\nRole: <@&" + roleID + ">")
				.build();
		channel.sendMessageEmbeds(newRoleEmbed).queue();
	}
	
	private void removeLevelUpRole(MessageChannel channel, List<String> args, Server server) {
		if (args.size() < 2) {
			channel.sendMessage("**You need to provide a level to remove the role from!**").queue();
			return;
		}
		
		String level = args.get(1);
		if (!ParsingUtils.isInteger(level)) {
			channel.sendMessage("**You need to provide a valid number for the level!**").queue();
			return;
		}
		
		if (!xpService.serverHasRoleForLevel(server.getId(), Integer.parseInt(level))) {
			channel.sendMessage("**There isn't a role set up for this level!**").queue();
			return;
		}
		
		xpService.removeLevelUpRole(Integer.parseInt(level), server.getId());
		MessageEmbed roleDeletedEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Removed Level Up Role!")
				.setDescription("Role for level `" + level + "` has been removed")
				.build();
		channel.sendMessageEmbeds(roleDeletedEmbed).queue();
	}

	private void showAllLevelRoles(MessageChannel channel, Server server, Guild guild) {
		List<LevelUpRole> levelUpRoles = xpService.getAllLevelUpRolesForServer(server.getId());
		if (levelUpRoles.isEmpty()) 
			channel.sendMessage("**There are no level roles set up in this server! See the help page for more info!**").queue();
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
			channel.sendMessageEmbeds(levelRolesEmbed).queue();
		}
	}

	private void sendLevelCard(MessageChannel channel, Member member, long serverID) {
		Experience userXP = xpService.getUserExperience(member.getId(), serverID);
		
		byte[] xpCard = xpService.getExperienceCard(member.getUser().getAvatarUrl(),
				userXP.getCurrentExperience(), xpService.xpNeededForLevel(userXP.getLevel() + 1),
				userXP.getLevel(), 1, member.getUser().getName() + "#" + member.getUser().getDiscriminator(),
				"#FF0000", "#2E2E2E");
		channel.sendFile(xpCard, "xpcard.png").queue();
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
