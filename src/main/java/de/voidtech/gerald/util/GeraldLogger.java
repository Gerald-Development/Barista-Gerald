package main.java.de.voidtech.gerald.util;

import java.awt.Color;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class GeraldLogger {
	
	private static final Logger LOCAL_LOGGER = Logger.getLogger(GeraldLogger.class.getName());
	
	public static String AvatarUrl;
	public static String WebhookURL;
	public static String BotName;
	
	private final String className;
	private final Logger logger;
	
	public GeraldLogger(String className) {
		this.className = className;
		this.logger = Logger.getLogger(className);
	}
	
	public void logWithoutWebhook(Level logLevel, String message) {
		this.logger.log(logLevel, message);
	}
	
	public void log(Level logLevel, String message) {
		this.logger.log(logLevel, message);
		if (WebhookURL != null) logMessageToWebhook(this.className, message, logLevel);
	}
	
	private static Color GetColourFromLevel(Level level) {
		switch (level.getName()) {
			case "INFO":
				return Color.GREEN;
			case "SEVERE":
				return Color.RED;
			case "WARNING":
				return Color.YELLOW;
		}
		return Color.BLUE;
	}
	
	private void logMessageToWebhook(String origin, String message, Level logLevel) {
		if (logLevel == Level.FINE) return;
		JSONObject payload = new JSONObject();
		payload.put("username", BotName + " status");
		payload.put("avatar_url", AvatarUrl);
		payload.put("tts", false);
		MessageEmbed logEmbed = new EmbedBuilder()
				.setColor(GetColourFromLevel(logLevel))
				.setTitle("[" + logLevel.getName() + "] " + origin)
				.setDescription("```\n" + message + "\n```")
				.build();
		payload.put("embeds", new JSONArray().put(new JSONObject(logEmbed.toData().toString())));
		postWebhook(payload);
	}
	
	private void postWebhook(JSONObject payload) {
        try {              			
            URL url = new URL(WebhookURL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Barista-Gerald");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            
            OutputStream stream = connection.getOutputStream();
            stream.write(payload.toString().getBytes());
            stream.flush();
            stream.close();

            connection.getInputStream().close();
            connection.disconnect();
        	
        } catch (Exception ex) {
        	LOCAL_LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + ex.getMessage());
            ex.printStackTrace();
        }
	}
}