package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ApodCommand extends AbstractCommand {

    private static final String API_URL = "https://api.nasa.gov/planetary/apod?api_key=lCJQbMiUG6iZMQdas8Qcg2IQ8KQmC19Ssuhc84pi";
    private static final String NASA_FOOTER = "https://cdn.freebiesupply.com/logos/large/2x/nasa-1-logo-png-transparent.png";
    private static final Logger LOGGER = Logger.getLogger(ApodCommand.class.getName());

    @Override
    public void executeInternal(Message message, List<String> args) {
        JSONObject response = getNasaDataOpt();
        
        EmbedBuilder nasaEmbed = new EmbedBuilder()
                .setTitle(response.getString("title"))
                .setColor(Color.ORANGE)
                .setFooter("Data from NASA", NASA_FOOTER);

        if (response.getString("media_type").equals("video")) {
            nasaEmbed.setTitle(response.getString("title"), response.getString("url"));
            nasaEmbed.setDescription("Click the title to view the video of the day :)");
        } else {
            nasaEmbed.setImage(response.getString("url"));
        }

        message.getChannel().sendMessage(nasaEmbed.build()).queue();
    }

    private JSONObject getNasaDataOpt() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    return new JSONObject(in.lines().collect(Collectors.joining()));
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
        return "Get NASA's Astronomy Picture Of the Day";
    }

    @Override
    public String getUsage() {
        return "apod";
    }
}
