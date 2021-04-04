package main.java.de.voidtech.gerald.service;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

@Service
public class NitroliteService {

	@Autowired
	WebhookManager webhookManager;
	
    public void sendMessage(Message originMessage, String content) {
    	
    	EnumSet<Permission> perms = originMessage.getGuild().getSelfMember().getPermissions((GuildChannel) originMessage.getChannel());
    	
        if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
           sendWebhookMessage(originMessage, content);
        } else {
            if (perms.contains(Permission.MESSAGE_MANAGE)) {
                originMessage.delete().complete();
                sendRegularMessage(originMessage, content, true);
            } else {
            	sendRegularMessage(originMessage, content, false);
            }
        }
    }
    
    private void sendRegularMessage(Message originMessage, String content, boolean canDeleteMessages) {
    	String finalMessage = "";
    	if (canDeleteMessages) {
    		finalMessage += "**" + originMessage.getAuthor().getAsTag() + "**: ";
    	}
    	 finalMessage += content;
    	originMessage.getChannel().sendMessage(finalMessage).queue();
	}

	public String constructEmoteString(Emote emote) {
        return String.format("<%s%s:%s>", emote.isAnimated() ? "a:" : ":", emote.getName(), emote.getId());
    }

    private void sendWebhookMessage(Message message, String content) {    	
    	Webhook webhook = webhookManager.getOrCreateWebhook((TextChannel) message.getChannel(), "BGNitrolite");
    	webhookManager.postMessage(content, message.getAuthor().getAvatarUrl(), message.getAuthor().getName(), webhook);
    	
        
    }
}
