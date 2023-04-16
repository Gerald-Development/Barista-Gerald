package main.java.de.voidtech.gerald.listeners;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.java.de.voidtech.gerald.persistence.entity.AutoroleConfig;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.AutoroleService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class AutoroleListener implements EventListener {
	
	@Autowired
	private AutoroleService autoroleService;
	
	@Autowired
	private ServerService serverService;
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof GuildMemberJoinEvent) {
			GuildMemberJoinEvent joinEvent = (GuildMemberJoinEvent) event;
			Server server = serverService.getServer(joinEvent.getGuild().getId());
			List<AutoroleConfig> configs = autoroleService.getAutoroleConfigs(server.getId());
			if (!configs.isEmpty()) {
				autoroleService.addRolesToMember((GuildMemberJoinEvent) event, configs);
			}
		}	
	}
}