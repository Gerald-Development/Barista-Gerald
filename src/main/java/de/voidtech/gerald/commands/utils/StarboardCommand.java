package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.persistence.entity.StarboardConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.StarboardService;
import main.java.de.voidtech.gerald.listeners.EventWaiter;
import main.java.de.voidtech.gerald.util.MRESameUserPredicate;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Command
public class StarboardCommand extends AbstractCommand {
    //TODO (from: Franziska): Needs some thinking and rewriting for SlashCommands. I will just not implement the context.reply just yet as it will confuse the refactoring later.
    @Autowired
    private ServerService serverService;

    @Autowired
    private StarboardService starboardService;

    @Autowired
    private EventWaiter waiter;

    private void getAwaitedReply(CommandContext context, String question, Consumer<String> result) {
        context.getChannel().sendMessage(question).queue();
        waiter.waitForEvent(MessageReceivedEvent.class,
                new MRESameUserPredicate(context.getAuthor()),
                event -> result.accept(event.getMessage().getContentRaw()), 30, TimeUnit.SECONDS,
                () -> context.getChannel().sendMessage("Request timed out.").queue());
    }

    private void setupStarboard(CommandContext context, Server server) {
        if (starboardService.getStarboardConfig(server.getId()) != null)
            context.getChannel().sendMessage("**A Starboard has already been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
        else {
            getAwaitedReply(context, "**Please enter a channel ID or mention to be used for the starboard:**", response -> {
                String channelID = ParsingUtils.filterSnowflake(response);
                if (!context.getGuild().getChannels().contains(context.getJDA().getGuildChannelById(channelID)))
                    context.getChannel().sendMessage("**The channel you provided is either invalid or not in this server!**").queue();
                else promptForStarCount(context, channelID, server);
            });
        }
    }

    private void promptForStarCount(CommandContext context, String channelID, Server server) {
        getAwaitedReply(context, "**Please enter the star count:**", response -> {
            if (!ParsingUtils.isInteger(response))
                context.getChannel().sendMessage("**You need to specify a number for the star count!**").queue();
            else if (Integer.parseInt(response) < 1)
                context.getChannel().sendMessage("**Your star count must be at least 1! We recommend 5**").queue();
            else starboardService.completeStarboardSetup(context, channelID, response, server);
        });
    }

    private void disableStarboard(CommandContext context, Server server) {
        if (starboardService.getStarboardConfig(server.getId()) != null)
            starboardService.deleteStarboardConfig(context, server);
        else
            context.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
    }

    private void changeChannel(CommandContext context, List<String> args, Server server) {
        if (starboardService.getStarboardConfig(server.getId()) != null) {
            String channelID = ParsingUtils.filterSnowflake(args.get(1));
            if (!context.getGuild().getChannels().contains(context.getJDA().getGuildChannelById(channelID)))
                context.getChannel().sendMessage("**The channel you provided is either invalid or not in this server!**").queue();
            else {
                StarboardConfig config = starboardService.getStarboardConfig(server.getId());
                config.setChannelID(channelID);
                starboardService.updateConfig(config);
                context.getChannel().sendMessage("**Message channel has been changed to <#" + channelID + ">!**").queue();
            }
        } else
            context.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
    }

    private void changeRequiredStarCount(CommandContext context, List<String> args, Server server) {
        if (starboardService.getStarboardConfig(server.getId()) != null) {
            if (!ParsingUtils.isInteger(args.get(1)))
                context.getChannel().sendMessage("**You need to specify a number for the star count!**").queue();
            else {
                StarboardConfig config = starboardService.getStarboardConfig(server.getId());
                config.setRequiredStarCount(Integer.parseInt(args.get(1)));
                starboardService.updateConfig(config);
                context.getChannel().sendMessage("**Required count has been changed to " + args.get(1) + "!**").queue();
            }
        } else
            context.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
    }

    private void showIgnoredChannels(CommandContext context, Server server) {
        if (starboardService.getStarboardConfig(server.getId()) != null) {
            List<String> ignoredChannels = starboardService.getIgnoredChannels(server.getId());
            String ignoredChannelMessage;
            if (ignoredChannels == null) {
                ignoredChannelMessage = "None ignored!";
            } else {
                StringBuilder ignoredChannelMessageBuilder = new StringBuilder();
                for (String id : ignoredChannels)
                    ignoredChannelMessageBuilder.append("<#").append(id).append(">\n");
                ignoredChannelMessage = ignoredChannelMessageBuilder.toString();
            }
            context.getChannel().sendMessageEmbeds(constructIgnoredChannelEmbed(ignoredChannelMessage)).queue();
        } else
            context.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
    }

    private void ignoreChannel(CommandContext context, List<String> args, Server server) {
        if (context.getMember().getPermissions().contains(Permission.MANAGE_CHANNEL)) {
            if (starboardService.getStarboardConfig(server.getId()) != null) {
                String channelID = ParsingUtils.filterSnowflake(args.get(1));
                if (!ParsingUtils.isSnowflake(channelID))
                    context.getChannel().sendMessage("**The channel you provided is not valid!**").queue();
                else if (!context.getGuild().getChannels().contains(context.getJDA().getGuildChannelById(channelID)))
                    context.getChannel().sendMessage("**The channel you provided is not in this server!**").queue();
                else {
                    if (starboardService.getIgnoredChannels(server.getId()) == null) {
                        starboardService.addChannelToIgnoreList(server.getId(), channelID);
                        context.getChannel().sendMessage("<#" + channelID + "> **has been added to the blacklist!**").queue();
                    } else if (starboardService.getIgnoredChannels(server.getId()).contains(channelID))
                        context.getChannel().sendMessage("**This channel is already blacklisted!**").queue();
                    else {
                        starboardService.addChannelToIgnoreList(server.getId(), channelID);
                        context.getChannel().sendMessage("<#" + channelID + "> **has been added to the blacklist!**").queue();
                    }
                }
            } else
                context.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
        }
    }

    private void unignoreChannel(CommandContext context, List<String> args, Server server) {
        if (context.getMember().getPermissions().contains(Permission.MANAGE_CHANNEL)) {
            if (starboardService.getStarboardConfig(server.getId()) != null) {
                String channelID = ParsingUtils.filterSnowflake(args.get(1));
                if (!ParsingUtils.isSnowflake(channelID))
                    context.getChannel().sendMessage("**The channel you provided is not valid!**").queue();
                else if (!context.getGuild().getChannels().contains(context.getJDA().getGuildChannelById(channelID)))
                    context.getChannel().sendMessage("**The channel you provided is not in this server!**").queue();
                else {
                    if (starboardService.getIgnoredChannels(server.getId()) == null)
                        context.getChannel().sendMessage("**There is no blacklist yet!**").queue();
                    else if (starboardService.getIgnoredChannels(server.getId()).contains(channelID)) {
                        starboardService.removeFromIgnoreList(server.getId(), channelID);
                        context.getChannel().sendMessage("<#" + channelID + "> **has been removed from the blacklist!**").queue();
                    } else
                        context.getChannel().sendMessage("**This channel is not yet blacklisted!**").queue();
                }
            } else
                context.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
        }
    }

    private MessageEmbed constructIgnoredChannelEmbed(String ignoredChannelMessage) {
        return new EmbedBuilder()
                .setTitle("Starboard Ignored Channels")
                .setColor(Color.ORANGE)
                .setDescription(ignoredChannelMessage)
                .build();
    }

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (context.getMember().getPermissions().contains(Permission.MANAGE_CHANNEL)) {
            Server server = serverService.getServer(context.getGuild().getId());

            switch (args.get(0)) {
                case "setup":
                    setupStarboard(context, server);
                    break;
                case "count":
                    changeRequiredStarCount(context, args, server);
                    break;
                case "channel":
                    changeChannel(context, args, server);
                    break;
                case "disable":
                    disableStarboard(context, server);
                    break;
                case "ignore":
                    ignoreChannel(context, args, server);
                    break;
                case "unignore":
                    unignoreChannel(context, args, server);
                    break;
                case "ignored":
                    showIgnoredChannels(context, server);
                    break;
                default:
                    context.getChannel().sendMessage("**That's not a valid subcommand! Try this instead:**\n\n" + this.getUsage()).queue();
            }
        } else
            context.getChannel().sendMessage("**You need the ** `Manage Channels` **Permission to do that!**").queue();
    }

    @Override
    public String getDescription() {
        return "Do you like quoting things? Funny, interesting and more? Perfect!\n"
                + "Our starboard system allows you to react to messages with the :star: emote and have them automatically sent to"
                + " a starboard channel in your server! Your server admins can choose the channel and number of stars needed to get it pinned!"
                + " Additionally, they may choose to ignore some channels. Ignore a channel with the ignore command, allow it again with the"
                + " unignore command, and show all the ignored channels with the ignored command!";
    }

    @Override
    public String getUsage() {
        return "starboard setup [Channel mention / ID] [Required star count]\n"
                + "starboard count [New number of stars needed]\n"
                + "starboard channel [New channel mention / ID]\n"
                + "starboard disable\n"
                + "starboard ignore [channel mention / ID]\n"
                + "starboard unignore [channel mention / ID]\n"
                + "starboard ignored\n\n"
                + "NOTE: You MUST run the setup command first!";
    }

    @Override
    public String getName() {
        return "starboard";
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
        return true;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"autoquote", "quotechannel", "sb"};
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
