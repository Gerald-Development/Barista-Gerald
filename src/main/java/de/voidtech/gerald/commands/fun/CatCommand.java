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
public class CatCommand extends AbstractCommand {

    private static final String API_URL = "https://api.thecatapi.com/v1/images/search";
    @Autowired
    private HttpClientService httpClientService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        JSONArray catApiResponse = httpClientService.getAndReturnJsonArray(API_URL);
        JSONObject cat = (JSONObject) catApiResponse.get(0);

        MessageEmbed catEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Here, have a cat", cat.getString("url"))
                .setImage(cat.getString("url"))
                .build();
        context.reply(catEmbed);
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
        return new String[]{"gato", "catto"};
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