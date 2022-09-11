package main.java.de.voidtech.gerald.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.util.GeraldLogger;

@Command
public class FactCommand extends AbstractCommand 
{
	private static final String REQUEST_URL = "https://uselessfacts.jsph.pl/random.json?language=en";
	private static final GeraldLogger LOGGER = LogService.GetLogger(FactCommand.class.getSimpleName());

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String factOpt = getFactOpt();
		if (factOpt != null) context.reply(factOpt);
	}
	
	private String getFactOpt() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(REQUEST_URL).openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String content = in.lines().collect(Collectors.joining());
					JSONObject json = new JSONObject(content);
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
	
	@Override
	public String[] getCommandAliases() {
        return new String[]{"uselessfact"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
	@Override
	public boolean isSlashCompatible() {
		return true;
	}

}
