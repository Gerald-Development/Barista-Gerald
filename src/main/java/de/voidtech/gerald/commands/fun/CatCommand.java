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
import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class CatCommand extends AbstractCommand{

	private static final String API_URL = "https://api.thecatapi.com/v1/images/search";
	private static final Logger LOGGER = Logger.getLogger(CatCommand.class.getName());
	
	private String getcat() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    return in.lines().collect(Collectors.joining());
                }
            }

            con.disconnect();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
        }
        return null;
    }
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		String catApiResponse = getcat();
		JSONArray catApiObject = new JSONArray(catApiResponse);
		JSONObject cat = (JSONObject) catApiObject.get(0);
		
		MessageEmbed catEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Here, have a cat", cat.getString("url"))
				.setImage(cat.getString("url"))
				.build();
		message.getChannel().sendMessage(catEmbed).queue();
	}

	@Override
	public String getDescription() {
		return "Searches the internet for a random cat image";
	}

	@Override
	public String getUsage() {
		return "cat";
	}

	@Override
	public String getName() {
		return "cat";
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
		String[] aliases = {};
		return aliases;
	}

}
