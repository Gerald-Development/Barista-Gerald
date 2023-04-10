package main.java.de.voidtech.gerald.service;

import java.awt.Color;
import java.time.Instant;
import java.util.Objects;

import main.java.de.voidtech.gerald.persistence.repository.JoinLeaveMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.persistence.entity.JoinLeaveMessage;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

@Service
public class JoinLeaveMessageService {
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private JoinLeaveMessageRepository repository;

	private boolean customMessageEnabled(long guildID) {
		return getJoinLeaveMessageEntity(guildID) != null;
	}
	
	private JoinLeaveMessage getJoinLeaveMessageEntity(long guildID) {
		return repository.getJoinLeaveMessageByServerId(guildID);
	}
	
	public void sendJoinMessage(GuildMemberJoinEvent event) {
		Server server = serverService.getServer(event.getGuild().getId());
		if (customMessageEnabled(server.getId())) {
			JoinLeaveMessage joinLeaveMessage = getJoinLeaveMessageEntity(server.getId());
			GuildChannel channel = event.getJDA().getGuildChannelById(joinLeaveMessage.getChannelID());
			String message = joinLeaveMessage.getJoinMessage();
			Member member = event.getMember();
			
			MessageEmbed joinMessageEmbed = new EmbedBuilder()
					.setColor(Color.green)
					.setDescription(member.getAsMention() + " **(" + member.getUser().getAsTag() + ") " + message + "**")
					.setTimestamp(Instant.now())
					.build();
			
			((MessageChannel) Objects.requireNonNull(channel)).sendMessageEmbeds(joinMessageEmbed).queue();
		}
	}
	
	public void sendLeaveMessage(GuildMemberRemoveEvent event) {
		Server server = serverService.getServer(event.getGuild().getId());
		if (customMessageEnabled(server.getId())) {
			JoinLeaveMessage joinLeaveMessage = getJoinLeaveMessageEntity(server.getId());
			GuildChannel channel = event.getJDA().getGuildChannelById(joinLeaveMessage.getChannelID());
			String message = joinLeaveMessage.getLeaveMessage();
			Member member = event.getMember();
			
			MessageEmbed leaveMessageEmbed = new EmbedBuilder()
					.setColor(Color.red)
					.setDescription(Objects.requireNonNull(member).getAsMention() + " **(" + member.getUser().getAsTag() + ") " + message + "**")
					.setTimestamp(Instant.now())
					.build();
			
			((MessageChannel) channel).sendMessageEmbeds(leaveMessageEmbed).queue();
		}
	}
	
}