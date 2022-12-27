package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.service.Mee6ExperienceImporter;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class ImportMee6Command extends AbstractCommand {

    @Autowired
    private Mee6ExperienceImporter mee6ExperienceImporter;

    @Autowired
    private GeraldConfig config;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (!config.getMasters().contains(context.getAuthor().getId())) {
            context.reply("**This command is currently only available to Gerald developers!**");
            return;
        }

        String guildId = args.isEmpty() ? context.getGuild().getId() : ParsingUtils.filterSnowflake(args.get(0));

        if (!ParsingUtils.isSnowflake(guildId)) {
            context.reply("**You need to provide a valid guild snowflake!**");
            return;
        }

        if (context.getJDA().getGuildById(guildId) == null) {
            context.reply("**Guild not found**");
            return;
        }

        if (!mee6ExperienceImporter.leaderboardExists(guildId)) {
            context.reply("**I couldn't find a MEE6 leaderboard for this server!**");
            return;
        }

        context.reply("**Importing MEE6 leaderboard. You will be alerted when this is finished!**");
        mee6ExperienceImporter.extractLeaderboardData(context, guildId);
    }

    @Override
    public String getDescription() {
        return "Allows server admins to migrate a MEE6 XP leaderboard (including level roles) over to Gerald!";
    }

    @Override
    public String getUsage() {
        return "importmee6";
    }

    @Override
    public String getName() {
        return "importmee6";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.UTILS;
    }

    @Override
    public boolean isDMCapable() {
        return false;
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[] {"im6"};
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public boolean isSlashCompatible() {
        return false;
    }
}
