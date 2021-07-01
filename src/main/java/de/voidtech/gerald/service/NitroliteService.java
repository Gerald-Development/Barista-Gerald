package main.java.de.voidtech.gerald.service;

import java.util.EnumSet;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.NitroliteAlias;
import main.java.de.voidtech.gerald.entities.NitroliteEmote;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

@Service
public class NitroliteService {

	@Autowired
	private WebhookManager webhookManager;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private EmoteService emoteService;
	
	public boolean aliasExists(String name, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			NitroliteAlias alias = (NitroliteAlias) session.createQuery("FROM NitroliteAlias WHERE ServerID = :serverID AND aliasName = :aliasName")
                    .setParameter("serverID", serverID)
                    .setParameter("aliasName", name)
                    .uniqueResult();
			return alias != null;
		}
	}
	
    public NitroliteEmote getEmoteFromAlias(String name, long serverID, Message message) {
    	try(Session session = sessionFactory.openSession())
		{
			NitroliteAlias alias = (NitroliteAlias) session.createQuery("FROM NitroliteAlias WHERE ServerID = :serverID AND aliasName = :aliasName")
                    .setParameter("serverID", serverID)
                    .setParameter("aliasName", name)
                    .uniqueResult();
			
			return emoteService.getEmoteById(alias.getEmoteID(), message.getJDA());
		}
	}
    
    public void deleteAliasesUsingEmote(String emoteID) {
    	try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM NitroliteAlias WHERE emoteID = :emoteID")
				.setParameter("emoteID", emoteID)
				.executeUpdate();
			session.getTransaction().commit();
		}
    }
	
    public void sendMessage(Message originMessage, String content) {
    	
    	EnumSet<Permission> perms = originMessage.getGuild().getSelfMember().getPermissions((GuildChannel) originMessage.getChannel());
    	
		if (originMessage.getAttachments().size() != 0) {
			for (Attachment attachment: originMessage.getAttachments()) {
				content = content + "\n" + attachment.getUrl();
			}	
		}
    	
        if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
           sendWebhookMessage(originMessage, content);
           if (perms.contains(Permission.MESSAGE_MANAGE))
               originMessage.delete().complete();
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

	public String constructEmoteString(NitroliteEmote emote) {
		if (emote == null) return "[Emote Deleted]";
		else return String.format("<%s%s:%s>", emote.isEmoteAnimated() ? "a:" : ":", emote.getName(), emote.getID());
    }

    private void sendWebhookMessage(Message message, String content) {    	
    	Webhook webhook = webhookManager.getOrCreateWebhook((TextChannel) message.getChannel(), "BGNitrolite", message.getJDA().getSelfUser().getId());
    	webhookManager.postMessage(content, message.getAuthor().getAvatarUrl(), message.getMember().getEffectiveName(), webhook); 
    }
}
