package main.java.de.voidtech.gerald.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;

public class InspiroCommand extends AbstractCommand{
	private static final String REQUEST_URL = "https://inspirobot.me/api?generate=true";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";
	private static final Logger LOGGER = Logger.getLogger(InspiroCommand.class.getName());

	@Override
	public void executeInternal(Message message, List<String> args) {
		String inspiroImageURLOpt = getInspiroImageURLOpt();
		if (inspiroImageURLOpt == null) super.sendErrorOccurred();
		else message.getChannel().sendMessage(inspiroImageURLOpt).queue();
	}
	
	private String getInspiroImageURLOpt() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(REQUEST_URL).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					return in.lines().collect(Collectors.joining());
				}
			}
			con.disconnect();
		} catch (IOException e) {
			super.sendErrorOccurred();
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return null;
	}

	@Override
	public String getDescription() {
		return "sends a very inspiring picture";
	}

	@Override
	public String getUsage() {
		return "inspiro";
	}

}
