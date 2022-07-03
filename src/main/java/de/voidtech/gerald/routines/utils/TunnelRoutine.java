package main.java.de.voidtech.gerald.routines.utils;

import java.util.EnumSet;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.Tunnel;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.NitroliteService;
import main.java.de.voidtech.gerald.service.WebhookManager;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

@Routine
public class TunnelRoutine extends AbstractRoutine {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private WebhookManager webhookManager;
	
	@Autowired
	private NitroliteService nitroliteService;
	
	private boolean tunnelExists(String senderChannelID) {
		
		try(Session session = sessionFactory.openSession())
		{
			Tunnel tunnel = (Tunnel) session.createQuery("FROM Tunnel WHERE sourceChannelID = :senderChannelID OR destChannelID = :senderChannelID")
                    .setParameter("senderChannelID", senderChannelID)
                    .uniqueResult();
			return tunnel != null;
		}
	}
	
	private boolean targetChannelExists(Tunnel tunnel, Message message) {
		String sourceChannelId = message.getChannel().getId();
		String targetChannelId = tunnel.getSourceChannel().equals(sourceChannelId)
				? tunnel.getDestChannel() 
				: tunnel.getSourceChannel();
		
		return message.getJDA().getTextChannelById(targetChannelId) != null;
	}
	
	private Tunnel getTunnel(String senderChannelID) {
		try(Session session = sessionFactory.openSession())
		{
            return (Tunnel) session.createQuery("FROM Tunnel WHERE sourceChannelID = :senderChannelID OR destChannelID = :senderChannelID")
.setParameter("senderChannelID", senderChannelID)
.uniqueResult();
		}
	}
	
	private void sendWebhookMessage(Webhook webhook, String content, Message message) {
		if (message.getAttachments().size() != 0) {
			StringBuilder contentBuilder = new StringBuilder(content);
			for (Attachment attachment: message.getAttachments()) {
				contentBuilder.append("\n").append(attachment.getUrl());
			}
			content = contentBuilder.toString();
		}
	
		webhookManager.postMessage(content, message.getReferencedMessage(), message.getAuthor().getAvatarUrl(), message.getAuthor().getName(), webhook);
	}
	
	private void sendTunnelMessage(Tunnel tunnel, Message message) {
		String messageContent = ParsingUtils.removeVolatileMentions(message.getContentRaw());
		TextChannel channel = tunnel.getSourceChannel().equals(message.getChannel().getId())
				? message.getJDA().getTextChannelById(tunnel.getDestChannel()) 
				: message.getJDA().getTextChannelById(tunnel.getSourceChannel());
		
		List<String> processedNitroliteMessage = nitroliteService.processNitroliteMessage(message);
		if (processedNitroliteMessage != null) messageContent = String.join(" ", processedNitroliteMessage);
		
		EnumSet<Permission> perms = channel.getGuild().getSelfMember().getPermissions(channel);
		if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
			Webhook webhook = webhookManager.getOrCreateWebhook(channel, "BGTunnel", message.getJDA().getSelfUser().getId());
			sendWebhookMessage(webhook, messageContent, message);
		} else channel.sendMessage("**" + message.getAuthor().getAsTag() + ":** " + messageContent).queue();
	}
	
	private void deleteTunnel(String channelId) {
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();
			session.createQuery("DELETE FROM Tunnel WHERE sourceChannelID = :channelID OR destChannelID = :channelID")
					.setParameter("channelID", channelId).executeUpdate();
			session.getTransaction().commit();
		}		
	}
	
	private void deleteTunnelAndSendError(Message message) {
		message.getChannel().sendMessage("**This tunnel no longer exists! It has now been deleted.**").queue();
		deleteTunnel(message.getChannel().getId());
	}

	@Override
	public void executeInternal(Message message) {
		if (message.getAuthor().getId().equals(message.getJDA().getSelfUser().getId())) return;
		if (tunnelExists(message.getChannel().getId())) {
			Tunnel tunnel = getTunnel(message.getChannel().getId());
			if (targetChannelExists(tunnel, message))
				sendTunnelMessage(getTunnel(message.getChannel().getId()), message);				
			else
				deleteTunnelAndSendError(message);
		}
	}

	@Override
	public String getDescription() {
		return "Allows tunnels to be handled";
	}

	@Override
	public boolean allowsBotResponses() {
		return true;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	@Override
	public String getName() {
		return "r-tunnel";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}
}