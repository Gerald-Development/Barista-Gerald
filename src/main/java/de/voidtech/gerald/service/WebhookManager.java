package main.java.de.voidtech.gerald.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class WebhookManager {

    private static final Logger LOGGER = Logger.getLogger(WebhookManager.class.getName());
	
	public Webhook getOrCreateWebhook(TextChannel targetChannel, String webhookName) {
		
		List<Webhook> webhooks = targetChannel.retrieveWebhooks().complete();
		for (Webhook webhook : webhooks) {
			if (webhook.getName().equals(webhookName)) {
				return webhook;
			}
		}
		Webhook newWebhook = targetChannel.createWebhook(webhookName).complete();
		return newWebhook;		
	}
	
	public void postMessage(String content, String avatarUrl, String username, Webhook webhook) {
		JSONObject webhookPayload = new JSONObject();
        webhookPayload.put("content", content);
        webhookPayload.put("username", username);
        webhookPayload.put("avatar_url", avatarUrl);
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
                LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " 
                + "Webhook " + webhook.getName() + " from " + webhook.getChannel().getName()
                + " Did not respond with 204");

            }
            response.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + ex.getMessage());
        }
	}
	
}
