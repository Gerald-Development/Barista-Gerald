package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONException;
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

public class ActionsCommand extends AbstractCommand {

    private static final Logger LOGGER = Logger.getLogger(ActionsCommand.class.getName());
    private static final String API_BASE_URL = "http://api.nekos.fun:8080/api/";
    private String apiURL;
    private String name;

    public ActionsCommand(String name) {
        this.apiURL =  API_BASE_URL + name;
        this.name = name;
    }

    @Override
    public void executeInternal(Message message, List<String> args) {
        if(message.getMentionedMembers().isEmpty()) {
            message.getChannel().sendMessage("You need to mention someone to " + this.name).queue();
        } else {
            String gifURL = getActionGif();
            if (gifURL == null)
                super.sendErrorOccurred();
            else {
                MessageEmbed actionEmbed = new EmbedBuilder()
                        .setTitle(message.getAuthor().getName() + " " + this.name + this.name.charAt(this.name.length()-1) +
                                "ed " + message.getMentionedMembers().get(0).getEffectiveName())
                        .setColor(Color.ORANGE)
                        .setImage(gifURL)
                        .build();
                message.getChannel().sendMessage(actionEmbed).queue();
            }
        }
    }

    private String getActionGif() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(apiURL).openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String content = in.lines().collect(Collectors.joining());
                    JSONObject json = new JSONObject(content);
                    return json.getString("image");
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
        return this.name + " someone";
    }

    @Override
    public String getUsage() {
        return this.name + "[@user#1234]";
    }
}
