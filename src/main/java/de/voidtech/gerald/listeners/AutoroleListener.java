package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.persistence.entity.AutoroleConfig;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.AutoroleService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Listener
public class AutoroleListener extends ListenerAdapter {
	
	@Autowired
	private AutoroleService autoroleService;
	
	@Autowired
	private ServerService serverService;

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		Server server = serverService.getServer(event.getGuild().getId());
		List<AutoroleConfig> configs = autoroleService.getAutoroleConfigs(server.getId());
		if (!configs.isEmpty()) {
			autoroleService.addRolesToMember(event, configs);
		}
	}
}