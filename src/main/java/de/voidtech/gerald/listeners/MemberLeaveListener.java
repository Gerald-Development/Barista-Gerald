package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.service.JoinLeaveMessageService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class MemberLeaveListener extends ListenerAdapter {

    @Autowired
    private JoinLeaveMessageService joinLeaveMessageService;

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        joinLeaveMessageService.sendLeaveMessage(event);
    }

}
