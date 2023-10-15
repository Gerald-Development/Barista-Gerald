package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class GuildGoneListener extends ListenerAdapter {

    @Autowired
    private ServerService serverService;

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        serverService.deleteServer(serverService.getServer(event.getGuild().getId()));
    }
}