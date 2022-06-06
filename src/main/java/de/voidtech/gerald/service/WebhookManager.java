package main.java.de.voidtech.gerald.service;

import java.awt.Color;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.GeraldLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

@Service
public class WebhookManager {
	
	@Autowired
	private ThreadManager threadManager;
	
	private static final GeraldLogger LOGGER = LogService.GetLogger(WebhookManager.class.getSimpleName());
	
	public Webhook getOrCreateWebhook(TextChannel targetChannel, String webhookName, String selfID) {
		
		List<Webhook> webhooks = targetChannel.retrieveWebhooks().complete();
		for (Webhook webhook : webhooks) {
			if (webhook.getName().equals(webhookName) && webhook.getOwnerAsUser().getId().equals(selfID)) {
				return webhook;
			}
		}
		return targetChannel.createWebhook(webhookName).complete();
	}
	
	private void executeWebhookPost(String content, Message referencedMessage, String avatarUrl, String username, Webhook webhook) {
		String messageToBeSent = content.replaceAll("@everyone", "``@``everyone").replaceAll("@here", "``@``here");
		
		JSONObject webhookPayload = new JSONObject();
        webhookPayload.put("content", messageToBeSent);
        webhookPayload.put("username", username);
        webhookPayload.put("avatar_url", avatarUrl);
        webhookPayload.put("tts", false);
        
		
		if (referencedMessage != null) {
			 MessageEmbed replyTextEmbed = new EmbedBuilder()
					.setTitle("Replying to this message", referencedMessage.getJumpUrl())
					.setDescription(referencedMessage.getContentRaw())
					.setColor(new Color(47,49,54))
					.setFooter("Original message from " + referencedMessage.getAuthor().getName(), referencedMessage.getAuthor().getAvatarUrl())
					.build();
			 webhookPayload.put("embeds", new JSONArray().put(new JSONObject(replyTextEmbed.toData().toString())));
		}
        
        try {              			
            URL url = new URL(webhook.getUrl());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Barista-Gerald");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            
            OutputStream stream = connection.getOutputStream();
            stream.write(webhookPayload.toString().getBytes());
            stream.flush();
            stream.close();

            connection.getInputStream().close();
            connection.disconnect();
        	
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + ex.getMessage());
            ex.printStackTrace();
        }
	}
	
	public void postMessage(String content, Message referencedMessage, String avatarUrl, String username, Webhook webhook) {
        Runnable webhookThreadRunnable = () -> executeWebhookPost(content, referencedMessage, avatarUrl, username, webhook);
		threadManager.getThreadByName("T-Webhook").execute(webhookThreadRunnable);   
	}
	
	public void postMessageWithFallback(CommandContext context, String content, String avatarUrl, String username, String webhookName) {
		EnumSet<Permission> perms = context.getGuild().getSelfMember().getPermissions((GuildChannel) context.getChannel());
		
        if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
        	postMessage(content, null,	avatarUrl, username,
        			getOrCreateWebhook((TextChannel) context.getChannel(),
        					webhookName,
        					context.getJDA().getSelfUser().getId())
        	);
         } else {
             context.getChannel().sendMessage(content).queue();
         }
	}
	
}
