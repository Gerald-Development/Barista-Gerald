package main.java.de.voidtech.gerald.commands;

import main.java.de.voidtech.gerald.exception.HandledGeraldException;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.AlarmSenderService;
import main.java.de.voidtech.gerald.service.MultithreadingService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.BCESameUserPredicate;
import main.java.de.voidtech.gerald.listeners.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractCommand {

    private static final Logger LOGGER = Logger.getLogger(AbstractCommand.class.getSimpleName());

    private static final String TRUE_EMOTE = "\u2705";
    private static final String FALSE_EMOTE = "\u274C";

    @Autowired
    private ServerService serverService;

    @Autowired
    private MultithreadingService multithreadingService;

    @Autowired
    private AlarmSenderService alarmService;

    @Autowired
    private EventWaiter waiter;

    private boolean runCommandInThread(CommandContext context, List<String> args) {
        if (!context.isMaster() && getCommandCategory().equals(CommandCategory.INVISIBLE)) return false;
        if (context.getChannel().getType() == ChannelType.PRIVATE && !this.isDMCapable()) {
            context.getChannel().sendMessage("**You can only use this command in guilds!**").queue();
        } else if (this.requiresArguments() && args.isEmpty()) {
            context.getChannel().sendMessage("**This command needs arguments to work! See the help command for more details!**\n" + this.getUsage()).queue();
        } else {
            Runnable commandThreadRunnable = () -> tryRunCommand(context, args);
            multithreadingService.getThreadByName("T-Command").execute(commandThreadRunnable);
        }
        return true;
    }

    private void tryRunCommand(CommandContext context, List<String> args) {
        try {
            executeInternal(context, args);
        } catch (Exception e) {
            String ref = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Command execution failed: " + e.getMessage() + " - Reference " + ref);
            alarmService.sendCommandAlarm(this.getName(), ref, e);
            if (!(e instanceof HandledGeraldException)) {
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

    private void getOverrideOption(CommandContext context, String location, List<ItemComponent> actions, Consumer<ButtonInteractionEvent> result) {
        Message m = context.getAuthor().openPrivateChannel().complete()
                .sendMessage("The command you wanted to run isn't enabled in **" + location + "**, run it anyway?").setActionRow(actions).complete();
        waiter.waitForEvent(ButtonInteractionEvent.class,
                new BCESameUserPredicate(context.getAuthor()),
                event -> {
                    if (!event.isAcknowledged()) {
                        event.deferEdit().queue();
                        event.getMessage().editMessageComponents().queue();
                    }
                    result.accept(event);
                }, 60, TimeUnit.SECONDS,
                () -> m.delete().queue());
    }

    private List<ItemComponent> createTrueFalseButtons() {
        List<ItemComponent> components = new ArrayList<>();
        components.add(Button.secondary("YES", TRUE_EMOTE));
        components.add(Button.secondary("NO", FALSE_EMOTE));
        return components;
    }

    public boolean run(CommandContext context, List<String> args) {
        if (context.getChannel().getType() == ChannelType.PRIVATE) {
            runCommandInThread(context, args);
        } else {
            Server server = serverService.getServer(context.getGuild().getId());
            Set<String> channelWhitelist = server.getChannelWhitelist();
            Set<String> commandBlacklist = server.getCommandBlacklist();

            boolean channelWhitelisted = channelWhitelist.isEmpty() || (channelWhitelist.contains(context.getChannel().getId()));
            boolean commandOnBlacklist = commandBlacklist.contains(getName());

            if ((channelWhitelisted && !commandOnBlacklist) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                return runCommandInThread(context, args);
            } else if (context.isMaster()) {
                String location = context.getGuild().getName() + " > " + context.getChannel().getName();
                getOverrideOption(context, location, createTrueFalseButtons(), event -> {
                    switch (event.getComponentId()) {
                        case "YES" -> {
                            event.getMessage().editMessage("Permissions overridden in **" + location + "**").queue();
                            LOGGER.log(Level.WARNING, context.getAuthor().getEffectiveName()
                                    + " used a permissions override in " + location);
                            runCommandInThread(context, args);
                        }
                        case "NO" -> event.getMessage().editMessage("Obeying the laws in **" + location + "**").queue();
                        default -> event.getMessage().editMessage("Something brokey :(").queue();
                    }
                });
                return true;
            }
        }
        return false;
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
