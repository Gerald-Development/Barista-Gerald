package main.java.de.voidtech.gerald.commands.info;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

public class WhoisCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		
		Member member = message.getMentionedMembers().size() >= 1// 
				? message.getMentionedMembers().get(0)//
				: message.getMember();
		
		List<String> memberRoles = member.getRoles().stream().map(Role::getAsMention).collect(Collectors.toList());
		
		MessageEmbed whoisEmbed = buildEmbed(member, memberRoles);
		
		message.getChannel().sendMessage(whoisEmbed).queue();
	}
	
	private MessageEmbed buildEmbed(Member member, List<String> memberRoles)
	{
		return new EmbedBuilder()//
				.setTitle("Who is " + member.getUser().getAsTag() + "?")//
				.setThumbnail(member.getUser().getAvatarUrl())
				.addField("Nickname:", member.getEffectiveName(), true)
				.addField("Status:", member.getOnlineStatus().toString(), true)
				.addField("Account created on:", member.getUser().getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), false)
				.addField("Server joined on:", member.getTimeJoined().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), true)
				.addField(String.format("Roles [%d]:", memberRoles.size()), StringUtils.join(memberRoles.toArray()), false)
				.addField("ID:", member.getId(), false)
				.build();
	}
	
	@Override
	public String getDescription() {
		return "returns information about the specified user";
	}

	@Override
	public String getUsage() {
		return "whois @BaristaBoi#4029";
	}
	
	@Override
	public Boolean getArgsRequirement() {
		return false;
	}

}
