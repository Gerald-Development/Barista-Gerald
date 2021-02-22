package main.java.de.voidtech.gerald.commands.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class EnlargeCommand extends AbstractCommand{
	
	private static final String CDN_URL = "https://cdn.discordapp.com/emojis/";
	private static final Logger LOGGER = Logger.getLogger(EnlargeCommand.class.getName());
	
	private boolean checkForPNG(String emoteID) {
		try {
			URL url = new URL(CDN_URL + emoteID + ".png");
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("HEAD");
			
			if (httpConnection.getResponseCode() == 415 || httpConnection.getResponseCode() == 404) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return false;
	}
	
	private boolean checkForGIF(String emoteID) {
		try {
			URL url = new URL(CDN_URL + emoteID + ".gif");
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("HEAD");
			
			if (httpConnection.getResponseCode() == 415 || httpConnection.getResponseCode() == 404) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
		LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());	
		}
		return false;
	}

	@Override
	public void executeInternal(Message message, List<String> args) {
		String emoteText = args.get(0);
		String regexPattern = "([^0-9])";
		String emoteID = emoteText.replaceAll(regexPattern, "");
		if (checkForGIF(emoteID)) {
			message.getChannel().sendMessage(CDN_URL + emoteID + ".gif").queue();
		} else if (checkForPNG(emoteID)) {
			message.getChannel().sendMessage(CDN_URL + emoteID + ".png").queue();
		} else {
			message.getChannel().sendMessage("Couldn't find that emote").queue();
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to send an enlarged image of an emote";
	}

	@Override
	public String getUsage() {
		return "enlarge [emote id/:emote:]";
	}

	@Override
	public String getName() {
		return "enlarge";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILS;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}

}
