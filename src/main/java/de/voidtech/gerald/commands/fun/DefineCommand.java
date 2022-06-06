package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.GeraldLogger;
import main.java.de.voidtech.gerald.service.LogService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class DefineCommand extends AbstractCommand{
	private final static String API_URL = "https://api.urbandictionary.com/v0/define?term=";
	private final static String REGEX = "[^a-zA-Z0-9()\"'?!;:., \\n]";
	private static final GeraldLogger LOGGER = LogService.GetLogger(DefineCommand.class.getSimpleName());
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String terms = String.join("+", args);
		String query = API_URL + terms;
		
		JSONArray definitions = getDefinition(query);
		
		if (definitions.length() == 0) {
			context.reply("That could not be defined!");
		} else {
			JSONObject definition = definitions.getJSONObject(0);
			MessageEmbed definitionEmbed = new EmbedBuilder()
					.setColor(Color.RED)
					.setTitle("Definition of " + definition.getString("word"))
					.addField("Definition", definition.getString("definition").replaceAll(REGEX, ""), true)
					.addField("Example", definition.getString("example").replaceAll(REGEX, ""), true)
					.setFooter("Definition by " + definition.getString("author"))
					.build();
			
			context.reply(definitionEmbed);
		}
	}
	
	private JSONArray getDefinition(String requestURL) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(requestURL).openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String content = in.lines().collect(Collectors.joining());
					JSONObject json = new JSONObject(content);
					return json.getJSONArray("list");
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
		return "looks up a definition on urban dictionary";
	}

	@Override
	public String getUsage() {
		return "define chad";
	}

	@Override
	public String getName() {
		return "define";
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
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		return new String[]{"ud", "urbandictionary"};
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
