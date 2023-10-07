package main.java.de.voidtech.gerald.commands.management;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Command
public class WhitelistCommand extends AbstractCommand {
    @Autowired
    private ServerService serverService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {

        if (!context.getMember().hasPermission(Permission.MANAGE_SERVER)) return;

        String argString = args.size() > 0 ? args.get(0) : "list";
        GuildChannel mentionedChannel = context.getMentionedChannels().size() > 0
                ? context.getMentionedChannels().get(0)
                : null;

        Server server = serverService.getServer(context.getGuild().getId());

        switch (argString) {
            case "add":
                handleAddToWhitelist(context, mentionedChannel, server);
                break;
            case "remove":
                handleRemoveFromWhitelist(context, mentionedChannel, server);
                break;
            case "clear":
                server.clearChannelWhitelist();
                context.reply("Whitelist has been cleared.");
                break;
            case "list":
                handleListWhitelist(context, server);
                break;
            default:
                context.reply(String.format("Please specify the command.\n```%s```", getUsage()));
        }

        serverService.saveServer(server);
    }


    private void handleAddToWhitelist(CommandContext context, GuildChannel mentionedChannel, Server server) {
        if (mentionedChannel != null && mentionedChannel.getGuild().getIdLong() == context.getGuild().getIdLong()) {
            if (server.getChannelWhitelist().contains(mentionedChannel.getId()))
                context.reply("This channel has already been added to the whitelist");
            else {
                server.addToChannelWhitelist(mentionedChannel.getId());
                context.reply("Channel has been added to the whitelist: " + mentionedChannel.getAsMention());
            }
        } else context.reply("Please provide a valid channel.");
    }

    private void handleRemoveFromWhitelist(CommandContext context, GuildChannel mentionedChannel, Server server) {
        if (mentionedChannel != null) {
            if (!server.getChannelWhitelist().contains(mentionedChannel.getId()))
                context.reply("This channel is not on the whitelist.");
            else {
                server.removeFromChannelWhitelist(mentionedChannel.getId());
                context.getChannel()
                        .sendMessage(
                                "Channel has been removed from the whitelist: " + mentionedChannel.getAsMention())
                        .queue();
            }
        } else context.reply("Please provide a valid channel.");
    }

    private void handleListWhitelist(CommandContext context, Server server) {
        List<String> channelList = context.getGuild()
                .getTextChannels()
                .stream()
                .filter(channel -> server.getChannelWhitelist().contains(channel.getId()))
                .map(IMentionable::getAsMention)
                .collect(Collectors.toList());

        String usableChannels = Objects.equals(StringUtils.join(channelList, "\n"), "") ? "All Channels" : StringUtils.join(channelList, "\n");

        context.reply(String.format("Gerald can be used in the following channels:\n%s",
                usableChannels));
    }

    @Override
    public String getDescription() {
        return "manages the whitelist of the server";
    }

    @Override
    public String getUsage() {
        return "whitelist add {channelID}\n"
                + "whitelist remove {channelID}\n"
                + "whitelist clear\n"
                + "whitelist";
    }

    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MANAGEMENT;
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
        return null;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean isSlashCompatible() {
        return true;
    }

}
