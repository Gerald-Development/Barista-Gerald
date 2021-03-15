package main.java.de.voidtech.gerald.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.java.de.voidtech.gerald.service.JoinLeaveMessageService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class MemberListener implements EventListener {
	
	@Autowired
	private JoinLeaveMessageService JLMService;
	
	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof GuildMemberJoinEvent) {
			GuildMemberJoinEvent joinEvent = (GuildMemberJoinEvent) event;
			JLMService.sendJoinMessage(joinEvent);
		}
		
		if (event instanceof GuildMemberRemoveEvent) {
			GuildMemberRemoveEvent leaveEvent = (GuildMemberRemoveEvent) event;
			JLMService.sendLeaveMessage(leaveEvent);
		}		
	}
}
