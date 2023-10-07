package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.InspiroService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class InspiroCommand extends AbstractCommand {

    @Autowired
    private InspiroService inspiroService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (args.isEmpty()) {
            String inspiroImageURLOpt = inspiroService.getInspiroImageURLOpt();
            if (inspiroImageURLOpt != null) {
                MessageEmbed inspiroEmbed = new EmbedBuilder()//
                        .setTitle("InspiroBot says:", inspiroImageURLOpt)//
                        .setColor(Color.ORANGE)//
                        .setImage(inspiroImageURLOpt)//
                        .setFooter("Data from InspiroBot", InspiroService.INSPIRO_ICON)//
                        .build();
                context.reply(inspiroEmbed);
            }
        } else if (args.get(0).equals("daily")) {
            if (!context.getMember().getPermissions().contains(Permission.MANAGE_CHANNEL)) {
                context.reply("**You need Manage Channels permissions to do that!**");
                return;
            }
            if (args.size() == 1) {
                context.reply("**You need to either specify a channel or use 'disable' to stop sending a daily inspiro**");
                return;
            }
            if (args.get(1).equals("disable")) {
                boolean disabled = inspiroService.disableDaily(context.getGuild().getId());
                context.reply(disabled ? "**Daily inspiro has been disabled**" : "**Daily inspiro was not enabled!**");
            } else {
                String parsedID = ParsingUtils.filterSnowflake(args.get(1));
                if (!ParsingUtils.isSnowflake(parsedID)) {
                    context.reply("**You need to supply a valid channel ID or mention!**");
                    return;
                }
                TextChannel channel = context.getGuild().getTextChannelById(parsedID);
                if (channel == null) {
                    context.reply("**That's not a valid channel!**");
                    return;
                }
                inspiroService.scheduleInspiro(context.getGuild().getId(), parsedID);
                context.reply("**Inspiration will now be sent daily to** <#" + parsedID + ">");
            }
        }
    }

    @Override
    public String getDescription() {
        return "Sends a very inspiring picture. To have one sent daily automatically in a channel of your choosing, use the 'daily' subcommand.";
    }

    @Override
    public String getUsage() {
        return "inspiro\n"
                + "inspiro daily [channel mention/ID]\n"
                + "inspiro daily disable";
    }

    @Override
    public String getName() {
        return "inspiro";
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
        return new String[]{"inspire", "inspirobot", "ib"};
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
