package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DefineCommand extends AbstractCommand{
	private final static String API_URL = "http://api.urbandictionary.com/v0/define?term=";
	private final static String REGEX = "[^a-zA-Z0-9()\"'?!;:., \\n]";
	private static final Logger LOGGER = Logger.getLogger(DefineCommand.class.getName());
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		String terms = String.join("+", args);
		String query = API_URL + terms;
		
		JSONArray definitions = getDefinition(query);
		
		if (definitions.length() == 0) {
			message.getChannel().sendMessage("That could not be defined!").queue();
		} else {
			JSONObject definition = definitions.getJSONObject(0);
			MessageEmbed definitionEmbed = new EmbedBuilder()
					.setColor(Color.RED)
					.setTitle("Definition of " + definition.getString("word"))
					.addField("Definition", definition.getString("definition").replaceAll(REGEX, ""), true)
					.addField("Example", definition.getString("example").replaceAll(REGEX, ""), true)
					.setFooter("Definition by " + definition.getString("author"))
					.build();
			
			message.getChannel().sendMessage(definitionEmbed).queue();			
		}
	}
	
	private JSONArray getDefinition(String requestURL) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(requestURL).openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String content = in.lines().collect(Collectors.joining());
					JSONObject json = new JSONObject(content.toString());
					return json.getJSONArray("list");
				}
			}
			con.disconnect();
		} catch (IOException | JSONException e) {
			super.sendErrorOccurred();
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

}
