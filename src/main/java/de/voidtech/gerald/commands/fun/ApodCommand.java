package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.HttpClientService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.Objects;

@Command
public class ApodCommand extends AbstractCommand {

    private static final String API_URL = "https://api.nasa.gov/planetary/apod?api_key=lCJQbMiUG6iZMQdas8Qcg2IQ8KQmC19Ssuhc84pi";
    private static final String NASA_FOOTER = "https://cdn.freebiesupply.com/logos/large/2x/nasa-1-logo-png-transparent.png";
    @Autowired
    private HttpClientService httpClientService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        JSONObject response = httpClientService.getAndReturnJson(API_URL);

        EmbedBuilder nasaEmbed = new EmbedBuilder()
                .setTitle(Objects.requireNonNull(response).getString("title"))
                .setColor(Color.ORANGE)
                .setFooter("Data from NASA", NASA_FOOTER);

        if (response.getString("media_type").equals("video")) {
            nasaEmbed.setTitle(response.getString("title"), response.getString("url"));
            nasaEmbed.setDescription("Click the title to view the video of the day :)");
        } else {
            nasaEmbed.setImage(response.getString("url"));
        }

        context.reply(nasaEmbed.build());
    }

    @Override
    public String getDescription() {
        return "Get NASA's Astronomy Picture Of the Day";
    }

    @Override
    public String getUsage() {
        return "apod";
    }

    @Override
    public String getName() {
        return "apod";
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
        return new String[]{"nasa", "nasaapod", "astronomy"};
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