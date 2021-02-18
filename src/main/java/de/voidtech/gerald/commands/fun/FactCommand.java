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

import org.json.JSONException;
import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class FactCommand extends AbstractCommand 
{
	private static final String REQUEST_URL = "https://uselessfacts.jsph.pl/random.json?language=en";
	private static final Logger LOGGER = Logger.getLogger(FactCommand.class.getName());

	@Override
	public void executeInternal(Message message, List<String> args) {
		String factOpt = getFactOpt();
		if (factOpt != null) message.getChannel().sendMessage(factOpt).queue();
	}
	
	private String getFactOpt() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(REQUEST_URL).openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String content = in.lines().collect(Collectors.joining());
					JSONObject json = new JSONObject(content.toString());
					return json.getString("text");
				}
			}
			con.disconnect();
		} catch (IOException | JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return null;
	}

	@Override
	public String getDescription() {
		return "slaps some random facts on the table";
	}

	@Override
	public String getUsage() {
		return "fact";
	}

	@Override
	public String getName() {
		return "fact";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}

}
