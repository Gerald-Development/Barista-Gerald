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
public class DefineCommand extends AbstractCommand {

    private final static String API_URL = "https://api.urbandictionary.com/v0/define?term=";
    private final static String REGEX = "[^a-zA-Z0-9()\"'?!;:., \\n]";
    @Autowired
    private HttpClientService httpClientService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        String terms = String.join("+", args);
        String query = API_URL + terms;

        JSONArray definitions = httpClientService.getAndReturnJson(query).getJSONArray("list");
        ;

        if (definitions.isEmpty()) {
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