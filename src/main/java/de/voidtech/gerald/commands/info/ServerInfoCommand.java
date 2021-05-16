package main.java.de.voidtech.gerald.commands.info;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class ServerInfoCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {

		Guild guild = message.getGuild();
		Member owner = guild.retrieveOwner().complete();


		MessageEmbed serverInfoEmbed = new EmbedBuilder()//
				.setTitle(guild.getName())
				.setThumbnail(guild.getIconUrl())
				.addField("Owner Information", String.format("```Owner ID: %s\nOwner Tag: %s```", owner.getId(), owner.getUser().getAsTag()), false)//
				.addField("General Server Info", String.format("```Server ID: %s\nCreated at: %s\nRegion: %s\nUser Verification Level: %s```", //
								guild.getId(),
								guild.getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
								guild.getRegion().getName(), guild.getVerificationLevel().name()),false)//
				.addField("Member Information", String.format("```Member count: %s\n```", guild.getMemberCount()), false)
				.addField("Server Boost Status", String.format("```Tier: %s\nBoost Count: %s", guild.getBoostTier(), guild.getBoostCount() + "```"), false)
				.build();

		message.getChannel().sendMessage(serverInfoEmbed).queue();

	}

	@Override
	public String getDescription() {
		return "returns information about the current server.";
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
		String[] aliases = {"si", "server"};
		return aliases;
	}

}
