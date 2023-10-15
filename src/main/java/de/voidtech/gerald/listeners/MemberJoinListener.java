package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.service.ExperienceService;
import main.java.de.voidtech.gerald.service.JoinLeaveMessageService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class MemberJoinListener extends ListenerAdapter {
	
	@Autowired
	private JoinLeaveMessageService joinLeaveMessageService;
	
	@Autowired
	private ExperienceService xpService;
	
	@Autowired
	private ServerService serverService;
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		joinLeaveMessageService.sendJoinMessage(event);
		xpService.addRolesOnServerJoin(serverService.getServer(event.getGuild().getId()), event.getMember());
	}
}