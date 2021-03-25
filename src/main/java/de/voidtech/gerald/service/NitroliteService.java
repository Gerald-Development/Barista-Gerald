package main.java.de.voidtech.gerald.service;

import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NitroliteService {
	private static volatile NitroliteService instance;
    private static final String PERMISSION_ERROR_MESSAGE_TEMPLATE = "Barista-Gerald does not have permissions to %s, contact the server owner to resolve this issue!";
    private static final Logger LOGGER = Logger.getLogger(NitroliteService.class.getName());

    //PRIVATE FOR SINGLETON
    private NitroliteService() {
    }

    public static NitroliteService getInstance() {
        if (NitroliteService.instance == null) {
            NitroliteService.instance = new NitroliteService();
        }
        return NitroliteService.instance;
    }

    public void sendMessage(Message originMessage, String content) {
    	
    	EnumSet<Permission> perms = originMessage.getGuild().getSelfMember().getPermissions((GuildChannel) originMessage.getChannel());
    	
        if (perms.contains(Permission.MANAGE_WEBHOOKS)) 
        {
            if (!sendWebhookMessage(originMessage, content)) 
            {
                ((TextChannel) originMessage.getChannel()).createWebhook("BGnitrolite").complete();

                if (!sendWebhookMessage(originMessage, content)) 
                {
                    LOGGER.log(Level.INFO, "Error during CommandExecution: Webhook creation has failed");
                } else attemptMessageDelete(originMessage, perms);
                
            } else attemptMessageDelete(originMessage, perms);
        } else 
        {
            if (perms.contains(Permission.MESSAGE_WRITE)) 
            {
                originMessage.getChannel().sendMessage(content).complete();

                attemptMessageDelete(originMessage, perms);
                
                sendErrorDM(originMessage, String.format(PERMISSION_ERROR_MESSAGE_TEMPLATE, "create webhooks in this channel"));
            } else 
            {
                sendErrorDM(originMessage, String.format(PERMISSION_ERROR_MESSAGE_TEMPLATE, "create webhooks or write in this channel"));
            }
        }
    }

    private void attemptMessageDelete(Message message, EnumSet<Permission> perms) {
        if (perms.contains(Permission.MESSAGE_MANAGE)) message.delete().queue();
        else sendErrorDM(message, String.format(PERMISSION_ERROR_MESSAGE_TEMPLATE, "delete messages in this channel"));
    }

    private void sendErrorDM(Message message, String text) {
        message.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(text)).queue();
    }

    public String constructEmoteString(Emote emote) {
        return String.format("<%s%s:%s>", emote.isAnimated() ? "a:" : ":", emote.getName(), emote.getId());
    }

    private boolean sendWebhookMessage(Message message, String content) {
        List<Webhook> webhooks = ((TextChannel) message.getChannel()).retrieveWebhooks().complete();

        for (Webhook webhook : webhooks) {
            if (webhook.getName().equals("BGnitrolite")) {
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
                    LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + ex.getMessage());
                }
                return true;
            }
        }
        return false;
    }
}
