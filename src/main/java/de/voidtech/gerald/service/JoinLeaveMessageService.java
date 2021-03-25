package main.java.de.voidtech.gerald.service;

import java.awt.Color;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.JoinLeaveMessage;
import main.java.de.voidtech.gerald.entities.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

@Service
public class JoinLeaveMessageService {
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private boolean customMessageEnabled(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			//TODO REVIEW: variable name not comforming code conventions.
			JoinLeaveMessage JLM = (JoinLeaveMessage) session.createQuery("FROM JoinLeaveMessage WHERE ServerID = :serverID")
                    .setParameter("serverID", guildID)
                    .uniqueResult();
			return JLM != null;
		}
	}
	
	private JoinLeaveMessage getJoinLeaveMessageEntity(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			//TODO REVIEW: variable name not comforming code conventions.
			JoinLeaveMessage JLM = (JoinLeaveMessage) session.createQuery("FROM JoinLeaveMessage WHERE ServerID = :serverID")
                    .setParameter("serverID", guildID)
                    .uniqueResult();
			return JLM;
		}
	}
	
	public void sendJoinMessage(GuildMemberJoinEvent event) {
		Server server = serverService.getServer(event.getGuild().getId());
		if (customMessageEnabled(server.getId())) {
			//TODO REVIEW: variable name not comforming code conventions.
			JoinLeaveMessage JLM = getJoinLeaveMessageEntity(server.getId());
			GuildChannel channel = event.getJDA().getGuildChannelById(JLM.getChannelID());
			String message = JLM.getJoinMessage();
			String member = event.getMember().getAsMention();
			
			MessageEmbed joinMessageEmbed = new EmbedBuilder()
					.setColor(Color.green)
					.setDescription(member + " **" + message + "**")
					.setTimestamp(null)
					.build();
			
			((MessageChannel) channel).sendMessage(joinMessageEmbed).queue();
		}
	}
	
	public void sendLeaveMessage(GuildMemberRemoveEvent event) {
		Server server = serverService.getServer(event.getGuild().getId());
		if (customMessageEnabled(server.getId())) {
			//TODO REVIEW: variable name not comforming code conventions.
			JoinLeaveMessage JLM = getJoinLeaveMessageEntity(server.getId());
			GuildChannel channel = event.getJDA().getGuildChannelById(JLM.getChannelID());
			String message = JLM.getLeaveMessage();
			String member = event.getUser().getAsMention();
			
			MessageEmbed leaveMessageEmbed = new EmbedBuilder()
					.setColor(Color.red)
					.setDescription(member + " **" + message + "**")
					.setTimestamp(null)
					.build();
			
			((MessageChannel) channel).sendMessage(leaveMessageEmbed).queue();
		}
	}
	
}
