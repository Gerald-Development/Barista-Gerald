package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.PlaywrightService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class JishoCommand extends AbstractCommand {

    private static final String JISHO_BASE_URL = "https://jisho.org/search/";
    @Autowired
    private PlaywrightService playwrightService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        context.getChannel().sendTyping().queue();
        String search = JISHO_BASE_URL + String.join("%20", args).replaceAll("#", "%23");
        byte[] resultImage = playwrightService.screenshotPage(search, 1500, 1500);
        //TODO (from: Franziska): Same as with queue. I probably need to proxy those things through?
        context.replyWithFile(resultImage, "screenshot.png", constructResultEmbed(search));
    }

    private MessageEmbed constructResultEmbed(String url) {
        return new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("**Your Search Result:**", url)
                .setImage("attachment://screenshot.png")
                .setFooter("Powered by Jisho")
                .build();
    }

    @Override
    public String getDescription() {
        return "Allows you to interact with Jisho! Jisho is a powerful Japanese-English dictionary. It lets you find words, kanji, example sentences and more quickly and easily. (https://jisho.org/)";
    }

    @Override
    public String getUsage() {
        return "jisho [something to search for]";
    }

    @Override
    public String getName() {
        return "jisho";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.INFO;
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
        return new String[]{"japanese"};
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
