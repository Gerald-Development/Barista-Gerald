package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.service.ExperienceService;
import main.java.de.voidtech.gerald.service.JoinLeaveMessageService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberListener implements EventListener {
	
	@Autowired
	private JoinLeaveMessageService JLMService;
	
	@Autowired
	private ExperienceService xpService;
	
	@Autowired
	private ServerService serverService;
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof GuildMemberJoinEvent) {
			GuildMemberJoinEvent joinEvent = (GuildMemberJoinEvent) event;
			JLMService.sendJoinMessage(joinEvent);
			xpService.addRolesOnServerJoin(serverService.getServer(joinEvent.getGuild().getId()), joinEvent.getMember());
			
		}
		
		if (event instanceof GuildMemberRemoveEvent) {
			GuildMemberRemoveEvent leaveEvent = (GuildMemberRemoveEvent) event;
			JLMService.sendLeaveMessage(leaveEvent);
		}		
	}
}