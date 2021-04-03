package main.java.de.voidtech.gerald.routines.utils;

import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.Tunnel;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Routine
public class TunnelRoutine extends AbstractRoutine {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private static final Logger LOGGER = Logger.getLogger(TunnelRoutine.class.getName());
	
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
			Tunnel tunnel = (Tunnel) session.createQuery("FROM Tunnel WHERE sourceChannelID = :senderChannelID OR destChannelID = :senderChannelID")
                    .setParameter("senderChannelID", senderChannelID)
                    .uniqueResult();
			return tunnel;
		}
	}
	
	private Webhook getOrCreateTunnelWebhook(TextChannel targetChannel) {
		
		Webhook returnHook = null;
		List<Webhook> webhooks = targetChannel.retrieveWebhooks().complete();
		for (Webhook webhook : webhooks) {
			if (webhook.getName().equals("BGTunnel")) {
				returnHook = webhook;
			}
		}
		if (returnHook == null) {
			returnHook = targetChannel.createWebhook("BGTunnel").complete();
		}
		return returnHook;		
	}
	
	private void sendWebhookMessage(Webhook webhook, String content, Message message) {
		
		if (message.getAttachments().size() != 0) {
			for (Attachment attachment: message.getAttachments()) {
				content = content + "\n" + attachment.getUrl();
			}	
		}
		
		JSONObject webhookPayload = new JSONObject();
        webhookPayload.put("content", content);
        webhookPayload.put("username", message.getAuthor().getName());
        webhookPayload.put("avatar_url", message.getAuthor().getAvatarUrl());
        webhookPayload.put("tts", false);

        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, webhookPayload.toString());
            Request request = new Request.Builder()
                    .url(webhook.getUrl())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            if (response.code() != 204) {
                response.close();
                throw new Exception("Webhook " + webhook.getName() + " from " + webhook.getChannel().getName()
                        + " did not respond with 204");

            }
            response.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error during RoutineExecution: " + ex.getMessage());
        }
	}
	
	private void sendTunnelMessage(Tunnel tunnel, Message message) {
			
		String messageContent = message.getContentRaw().replaceAll("@", "`@`");
		
		TextChannel channel = tunnel.getSourceChannel().equals(message.getChannel().getId())
				? message.getJDA().getTextChannelById(tunnel.getDestChannel()) 
				: message.getJDA().getTextChannelById(tunnel.getSourceChannel());
		
		EnumSet<Permission> perms = channel.getGuild().getSelfMember().getPermissions(channel);
		if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
			Webhook webhook = getOrCreateTunnelWebhook(channel);
			sendWebhookMessage(webhook, messageContent, message);
		} else {
			channel.sendMessage("**" + message.getAuthor().getAsTag() + ":** " + messageContent).queue();
		}
	}
	
	private void deleteTunnel(String channelId) {
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();
			session.createQuery(
					"DELETE FROM Tunnel WHERE sourceChannelID = :channelID OR destChannelID = :channelID")
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
			
			if (targetChannelExists(tunnel, message)) {
				sendTunnelMessage(getTunnel(message.getChannel().getId()), message);				
			} else {
				deleteTunnelAndSendError(message);
			}
		}
	}

	@Override
	public String getDescription() {
		return "The routine for handling tunnels";
	}

	@Override
	public boolean allowsBotResponses() {
		return true;
	}
}