package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@Command
public class ServerInfoCommand extends AbstractCommand {

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		Guild guild = context.getGuild();
		Member owner = guild.retrieveOwner().complete();
		List<Member> memberList = guild.loadMembers().get();
		context.getChannel().sendMessageEmbeds(createServerInfoEmbed(guild, owner, memberList)).queue();
	}

	private MessageEmbed createServerInfoEmbed(Guild guild, Member owner, List<Member> memberList) {
		int totalMemberCount = memberList.size();
		int botCount = (int) memberList.stream()//
				.map(Member::getUser)//
				.filter(User::isBot)//
				.count();
		int humanCount = totalMemberCount - botCount;
		int channelCount = guild.getChannels().size();
		int roleCount = guild.getRoles().size();
		int animatedEmoteCount = (int) guild.getEmotes().stream()
				.filter(Emote::isAnimated)
				.count();
		int staticEmoteCount = guild.getEmotes().size() - animatedEmoteCount;
		
		EmbedBuilder serverInfoEmbed = new EmbedBuilder()//
				.addField("Owner Information :crown:", getOwnerInformation(owner), false)//
				.addField("General Server Info :desktop_computer:", getGeneralInformation(guild, channelCount, roleCount),false)//
				.addField("Member Information :brain:", getMemberInformation(totalMemberCount, humanCount, botCount), false)//
				.addField("Guild Emotes :smile:", getGuildEmoteInformation(guild, animatedEmoteCount, staticEmoteCount), false)//
				.addField("Server Boost Status :rocket:", getServerBoostInformation(guild), false)//
				.setColor(Color.ORANGE)//
				.setThumbnail(guild.getIconUrl())//
				.setTitle(guild.getName());
		return serverInfoEmbed.build();
	}

	private String getServerBoostInformation(Guild guild) {
		return String.format("```Boost Tier: %s\nBoost Count: %s",
				getBoostTier(guild.getBoostTier().toString()), guild.getBoostCount() + "```");
	}

	private String getGuildEmoteInformation(Guild guild, int animatedEmoteCount, int staticEmoteCount) {
		return String.format("```Total Emote Count: %s\nAnimated Emote Count: %s\nStatic Emote Count: %s```",
				guild.getEmotes().size(), animatedEmoteCount, staticEmoteCount);
	}

	private String getMemberInformation(int totalMemberCount, int humanCount, int botCount) {
		return String.format("```Member count: %s\nHuman count: %s\nBot count: %s```", totalMemberCount, humanCount, botCount);
	}

	private String getGeneralInformation(Guild guild, int channelCount, int roleCount) {
		return String.format("```Server ID: %s\nCreated at: %s\nUser Verification Level: %s\nChannel Count: %s\nRole Count: %s```", 
				guild.getId(), guild.getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
				getVerificationLevel(guild.getVerificationLevel().name()), channelCount, roleCount);
	}

	private String getOwnerInformation(Member owner) {
		return String.format("```Owner ID: %s\nOwner Tag: %s```", owner.getId(), owner.getUser().getAsTag());
	}

	private String getBoostTier(String tier) {
		switch (tier) {
		case "TIER_1":
			return "Tier 1";
		case "TIER_2":
			return "Tier 2";
		case "TIER_3":
			return "Tier 3";
		default:
			return "No Boost Tier";
		}
	}
	
	private String getVerificationLevel(String level) {
		switch (level) {
		case "HIGH":
			return "High";
		case "LOW": 
			return "Low";
		case "MEDIUM":
			return "Medium";
		case "VERY HIGH":
			return "Very High";
		case "NONE":
			return "None";
		default:
			return "Unknown";
		}
	}

	@Override
	public String getDescription() {
		return "returns information about the server.";
	}

	@Override
	public String getUsage() {
		return "serverinfo";
	}

	@Override
	public String getName() {
		return "serverinfo";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.INFO;
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
		return new String[]{"si"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}