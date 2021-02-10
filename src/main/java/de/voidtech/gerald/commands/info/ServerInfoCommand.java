package main.java.de.voidtech.gerald.commands.info;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

@Command
public class ServerInfoCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {

		Guild guild = message.getGuild();
		Member owner = guild.retrieveOwner().complete();

		List<Member> memberList = guild.loadMembers().get();

		int totalMemberCount = memberList.size();
		int botCount = (int) memberList.stream()//
				.map(Member::getUser)//
				.filter(User::isBot)//
				.count();
		int humanCount = totalMemberCount - botCount;

		MessageEmbed serverInfoEmbed = new EmbedBuilder()//
				.setTitle(message.getGuild().getName())
				.addField("Owner Information", String.format("```Owner ID: %s\nOwner Tag: %s```", owner.getId(), owner.getUser().getAsTag()), false)//
				.addField("General Server Info", String.format("```Server ID: %s\nCreated at: %s\nRegion: %s\nUser Verification Level: %s```", //
								guild.getId(),
								guild.getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
								guild.getRegion().getName(), guild.getVerificationLevel().name()),false)//
				.addField("Member Information", String.format("```Member count: %s\nHuman count: %s\nBot count: %s```",totalMemberCount, humanCount, botCount), false)
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

}
