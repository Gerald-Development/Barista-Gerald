package main.java.de.voidtech.gerald.commands;

import main.java.de.voidtech.gerald.exception.UnhandledGeraldException;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.AlarmService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.MultithreadingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractCommand {

    private static final Logger LOGGER = Logger.getLogger(AbstractCommand.class.getSimpleName());

    @Autowired
    private ServerService serverService;

    @Autowired
    private MultithreadingService multithreadingService;

    @Autowired
    private AlarmService alarmService;

    private void runCommandInThread(CommandContext context, List<String> args) {
        if (!context.isMaster() && getCommandCategory().equals(CommandCategory.INVISIBLE)) return;
        if (context.getChannel().getType() == ChannelType.PRIVATE && !this.isDMCapable()) {
            context.getChannel().sendMessage("**You can only use this command in guilds!**").queue();
        } else if (this.requiresArguments() && args.isEmpty()) {
            context.getChannel().sendMessage("**This command needs arguments to work! See the help command for more details!**\n" + this.getUsage()).queue();
        } else {
            Runnable commandThreadRunnable = () -> tryRunCommand(context, args);
            multithreadingService.getThreadByName("T-Command").execute(commandThreadRunnable);
        }
    }

    private void tryRunCommand(CommandContext context, List<String> args) {
        try {
            executeInternal(context, args);
        } catch (Exception e) {
            String ref = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Command execution failed: " + e.getMessage() + " - Reference " + ref);
            alarmService.sendCommandAlarm(this.getName(), ref, context, e);
            if (e instanceof UnhandledGeraldException) {
                MessageEmbed errorEmbed = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle(":warning: Something has gone wrong :warning:")
                        .setDescription("Don't worry! The developers have been alerted to this error. Join our support server for more details")
                        .setFooter("Reference: " + ref)
                        .build();
                context.reply(errorEmbed);
            }
        }
    }

    public void run(CommandContext context, List<String> args) {
        if (context.getChannel().getType() == ChannelType.PRIVATE) {
            runCommandInThread(context, args);
        } else {
            Server server = serverService.getServer(context.getGuild().getId());
            Set<String> channelWhitelist = server.getChannelWhitelist();
            Set<String> commandBlacklist = server.getCommandBlacklist();

            boolean channelWhitelisted = channelWhitelist.isEmpty() || (channelWhitelist.contains(context.getChannel().getId()));
            boolean commandOnBlacklist = commandBlacklist.contains(getName());

            if ((channelWhitelisted && !commandOnBlacklist) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                runCommandInThread(context, args);
            }
        }
    }

    public abstract void executeInternal(CommandContext context, List<String> args);

    public abstract String getDescription();

    public abstract String getUsage();

    public abstract String getName();

    public abstract CommandCategory getCommandCategory();

    public abstract boolean isDMCapable();

    public abstract boolean requiresArguments();

    public abstract String[] getCommandAliases();

    public abstract boolean canBeDisabled();

    public abstract boolean isSlashCompatible();

}
