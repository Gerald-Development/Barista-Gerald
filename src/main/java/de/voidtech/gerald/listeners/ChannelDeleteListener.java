package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class ChannelDeleteListener extends ListenerAdapter {

    @Autowired
    private ServerService serverService;

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        Server server = serverService.getServer(event.getGuild().getId());
        server.removeFromChannelWhitelist(event.getChannel().getId());
        serverService.saveServer(server);
    }
}