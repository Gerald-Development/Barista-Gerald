package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class InfoCommand extends AbstractCommand {

    @Autowired
    private List<AbstractCommand> commands;
    @Autowired
    private List<AbstractRoutine> routines;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        long guildCount = context.getJDA().getGuildCache().size();
        long memberCount = context.getJDA().getGuildCache().stream().mapToInt(Guild::getMemberCount).sum();

        MessageEmbed informationEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Barista Gerald - A Java Discord Bot", GlobalConstants.LINKTREE_URL)
                .addField("Gerald Owner", "```elementalmp4```", false)
                .addField("People who made this happen", "```\n"
                        + "elementalmp4\r\n"
                        + "montori\r\n"
                        + "scot_survivor\r\n"
                        + "pagwin\r\n"
                        + "foxi```", false)

                .addField("Gerald Guild Count", "```" + guildCount + "```", true)
                .addField("Gerald Member Count", "```" + memberCount + "```", true)
                .addField("Active Threads", "```" + Thread.activeCount() + "```", true)
                .addField("Latest Release", "```" + GlobalConstants.VERSION + "```", false)
                .setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
                .setFooter("Command Count: " + commands.size() + "\nRoutine Count: " + routines.size(), context.getJDA().getSelfUser().getAvatarUrl())
                .build();
        context.reply(informationEmbed);
    }

    @Override
    public String getDescription() {
        return "Provides information about the Barista Gerald project and the developers who made it!";
    }

    @Override
    public String getUsage() {
        return "info";
    }

    @Override
    public String getName() {
        return "info";
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
        return false;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"botinfo", "botstats", "bi", "bs", "stats"};
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