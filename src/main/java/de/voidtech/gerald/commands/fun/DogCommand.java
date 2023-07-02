package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Command
public class DogCommand extends AbstractCommand{

	private static final String API_URL = "https://api.thedogapi.com/v1/images/search";
	private static final GeraldLogger LOGGER = LogService.GetLogger(DogCommand.class.getSimpleName());
	
	private String getDog() {
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
	public void executeInternal(CommandContext context, List<String> args) {
		String dogApiResponse = getDog();
		JSONArray dogApiObject = new JSONArray(Objects.requireNonNull(dogApiResponse));
		JSONObject dog = (JSONObject) dogApiObject.get(0);
		
		MessageEmbed dogEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Here, have a dog", dog.getString("url"))
				.setImage(dog.getString("url"))
				.build();
		context.reply(dogEmbed);
	}

	@Override
	public String getDescription() {
		return "Searches the internet for a random dog image";
	}

	@Override
	public String getUsage() {
		return "dog";
	}

	@Override
	public String getName() {
		return "dog";
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
		return new String[]{"doge", "doggo"};
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