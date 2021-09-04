package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.util.ParsingUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command
public class EnlargeCommand extends AbstractCommand {

	private static final String CDN_URL = "https://cdn.discordapp.com/emojis/";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";
	private static final Logger LOGGER = Logger.getLogger(EnlargeCommand.class.getName());
	private static final List<String> FILE_EXTENSIONS = Arrays.asList("gif", "png", "jpg", "jpeg");

	private boolean checkForImage(String emoteID, String extension) {
		try {
			URL url = new URL(CDN_URL + emoteID + "." + extension);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("HEAD");
			httpConnection.setRequestProperty("User-Agent", USER_AGENT);

			return (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return false;
	}

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String emoteText = args.get(0);
		String emoteID = "";
		
		if (emoteText.startsWith("<")) {
			emoteID = Arrays.asList(emoteText.split(":")).get(2).replace(">", "");
		} else {
			emoteID = ParsingUtils.filterSnowflake(emoteText);	
		}

		for (String extension : FILE_EXTENSIONS) {
			if (checkForImage(emoteID, extension)) {
				context.reply(CDN_URL + emoteID + "." + extension);
				return;
			}
		}
		context.reply("Couldn't find that emote");
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
	
	@Override
	public String[] getCommandAliases() {
        return new String[]{"jumbo"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}