package main.java.de.voidtech.gerald.commands.invisible;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.GeraldConfigService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.Objects;

@Command
public class CacheSearchCommand extends AbstractCommand {

    @Autowired
    private GeraldConfigService config;

    @Autowired
    private ServerService serverService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        JDA client = context.getJDA();
        String resultMessage = "";
        String imageURL = "";
        boolean foundItem = true;

        if (client.getGuildById(args.get(0)) != null) {
            Guild guild = client.getGuildById(args.get(0));
            Member owner = Objects.requireNonNull(guild).retrieveOwner().complete();
            resultMessage = "**Guild Cache**\n```\n"
                    + "Guild Name: " + guild.getName()
                    + "\nGuild ID: " + guild.getId()
                    + "\nGuild Owner: " + owner.getUser().getEffectiveName()
                    + "\nGuild Owner ID: " + owner.getId()
                    + "\nBarista Guild ID: " + serverService.getServer(guild.getId()).getId()
                    + "```";
            imageURL = guild.getIconUrl();

        } else if (client.getGuildChannelById(args.get(0)) != null) {
            GuildChannel channel = client.getGuildChannelById(args.get(0));
            resultMessage = "**Channel Cache**\n```\n"
                    + "Channel Name: " + Objects.requireNonNull(channel).getName()
                    + "\nChannel Type: " + channel.getType()
                    + "\nChannel ID: " + channel.getId()
                    + "\nGuild Name: " + channel.getGuild().getName()
                    + "\nGuild ID: " + channel.getGuild().getId()
                    + "\nBarista Guild ID: " + serverService.getServer(channel.getGuild().getId()).getId()
                    + "```";
            imageURL = channel.getGuild().getIconUrl();

        } else if (client.retrieveUserById(args.get(0)).complete() != null) {
            User user = client.retrieveUserById(args.get(0)).complete();
            resultMessage = "**User Cache**\n```\n"
                    + "Username: " + user.getEffectiveName()
                    + "\nUser ID: " + user.getId()
                    + "```";
            imageURL = user.getAvatarUrl();

        } else {
            foundItem = false;
        }

        if (foundItem) {
            MessageEmbed cacheSearchEmbed = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setThumbnail(imageURL)
                    .setDescription(resultMessage)
                    .build();
            context.reply(cacheSearchEmbed);
        } else {
            context.reply("**Nothing was found in the cache!**");
        }
    }

    @Override
    public String getDescription() {
        return "Allows bot masters to search the cache of members, channels and guilds";
    }

    @Override
    public String getUsage() {
        return "cachesearch [snowflake]";
    }

    @Override
    public String getName() {
        return "cachesearch";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.INVISIBLE;
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
        return new String[]{"cache", "csearch"};
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