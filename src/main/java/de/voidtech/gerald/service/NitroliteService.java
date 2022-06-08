package main.java.de.voidtech.gerald.service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

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
	
	@Autowired
	private ServerService serverService;
	
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
            StringBuilder contentBuilder = new StringBuilder(content);
            for (Attachment attachment: originMessage.getAttachments()) {
				contentBuilder.append("\n").append(attachment.getUrl());
			}
            content = contentBuilder.toString();
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
    	webhookManager.postMessage(content, message.getReferencedMessage(), message.getAuthor().getAvatarUrl(), message.getMember().getEffectiveName(), webhook); 
    }

	public List<String> processNitroliteMessage(Message message) {
		 List<String> messageTokens = Arrays.asList(message.getContentRaw().replaceAll("(?<! )\\[:", " \\[:").replaceAll(":\\](?! )", "\\:] ").split(" "));
	     long serverID = serverService.getServer(message.getGuild().getId()).getId();
	     boolean foundOne = false;

	     for (int i = 0; i < messageTokens.size(); i++) {
	         String token = messageTokens.get(i);
	         NitroliteEmote emoteOpt = null;
	         if (token.matches("\\[:[^:]*:]")) {
	             String searchWord = token.substring(2, token.length() - 2);    	
	             if (aliasExists(searchWord, serverID)) emoteOpt = getEmoteFromAlias(searchWord, serverID, message);
	             else emoteOpt = emoteService.getEmoteByName(searchWord, message.getJDA());

	             if (emoteOpt != null) {
	                 foundOne = true;
	                 messageTokens.set(i, constructEmoteString(emoteOpt));
	             }
	         }
	     }
	     return foundOne ? messageTokens : null; 
	}
}
