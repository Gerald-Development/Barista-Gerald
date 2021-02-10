package main.java.de.voidtech.gerald.commands;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class ActionsCommand extends AbstractCommand {
	
	private static final String API_URL = "http://api.nekos.fun:8080/api/";
	private static final Logger LOGGER = Logger.getLogger(ActionsCommand.class.getName());

	public void sendAction(Message message, String action) {
		if(message.getMentionedMembers().isEmpty()) {
            message.getChannel().sendMessage("You need to mention someone to " + action).queue();
        } else {
            String gifURL = getActionGif(action);
            if (gifURL != null)
            {
            	String phrase = message.getMember().getEffectiveName()//
            			+ " "//
            			+ conjugateAction(action)
            			+ message.getMentionedMembers().get(0).getEffectiveName();
            	
                MessageEmbed actionEmbed = new EmbedBuilder()
                        .setTitle(phrase)
                        .setColor(Color.ORANGE)
                        .setImage(gifURL)
                        .build();
                message.getChannel().sendMessage(actionEmbed).queue();
            }
        }
	}
	
	private String conjugateAction(String action) {
		String conjugatedAction = action;
		
		if (action.charAt(action.length() - 1) == action.charAt(action.length() - 2)) {
			conjugatedAction += "ed";
		}
		else if (action.charAt(action.length() - 1) == 'e') {
			conjugatedAction += "d";
		}
		else conjugatedAction += action.charAt(action.length()-1) + "ed";

		return conjugatedAction;
	}
	
	private String getActionGif(String action) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(API_URL + action).openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String content = in.lines().collect(Collectors.joining());
					JSONObject json = new JSONObject(content);
					return json.getString("image");
				}
			}
			con.disconnect();
		} catch (IOException | JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return null;
	}
}
