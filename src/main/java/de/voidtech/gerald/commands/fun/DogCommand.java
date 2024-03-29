package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.HttpClientService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class DogCommand extends AbstractCommand {

    private static final String API_URL = "https://api.thedogapi.com/v1/images/search";
    @Autowired
    private HttpClientService httpClientService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        JSONArray dogApiResponse = httpClientService.getAndReturnJsonArray(API_URL);
        JSONObject dog = (JSONObject) dogApiResponse.get(0);

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