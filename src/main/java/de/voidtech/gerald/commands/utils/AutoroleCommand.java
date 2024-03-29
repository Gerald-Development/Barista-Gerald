package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.AutoroleConfig;
import main.java.de.voidtech.gerald.service.AutoroleService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.BCESameUserPredicate;
import main.java.de.voidtech.gerald.listeners.EventWaiter;
import main.java.de.voidtech.gerald.util.MRESameUserPredicate;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Command
public class AutoroleCommand extends AbstractCommand {

    private static final String TRUE_EMOTE = "\u2705";
    private static final String FALSE_EMOTE = "\u274C";

    @Autowired
    private AutoroleService autoroleService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private EventWaiter waiter;

    private void getAwaitedReply(CommandContext context, Consumer<String> result) {
        context.getChannel().sendMessage("**Please supply a role mention or ID:**").queue();
        waiter.waitForEvent(MessageReceivedEvent.class,
                new MRESameUserPredicate(context.getAuthor()),
                event -> result.accept(event.getMessage().getContentRaw()), 30, TimeUnit.SECONDS,
                () -> context.getChannel().sendMessage("Request timed out.").queue());
    }

    private void getAwaitedButton(CommandContext context, String question, List<ItemComponent> actions, Consumer<ButtonInteractionEvent> result) {
        context.getChannel().sendMessage(question).setActionRow(actions).queue();
        waiter.waitForEvent(ButtonInteractionEvent.class,
                new BCESameUserPredicate(context.getAuthor()),
                event -> {
                    if (!event.isAcknowledged()) event.deferEdit().queue();
                    result.accept(event);
                }, 30, TimeUnit.SECONDS,
                () -> context.getChannel().sendMessage("Request timed out.").queue());
    }

    private void showAutoroles(CommandContext context) {
        List<AutoroleConfig> configs = autoroleService.getAutoroleConfigs(serverService.getServer(context.getGuild().getId()).getId());
        if (configs.isEmpty()) context.reply("**No autoroles to show!**");
        else {
            context.reply(craftAutoroleEmbed(configs, context));
        }
    }

    private MessageEmbed craftAutoroleEmbed(List<AutoroleConfig> configs, CommandContext context) {
        EmbedBuilder autoroleEmbedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Autoroles for " + context.getGuild().getName());
        StringBuilder messageBody = new StringBuilder();
        for (AutoroleConfig config : configs) {
            messageBody.append(addAutoroleField(config));
        }
        autoroleEmbedBuilder.setDescription(messageBody.toString());
        return autoroleEmbedBuilder.build();
    }

    private String addAutoroleField(AutoroleConfig config) {
        return String.format("**<@&%s>**\n```Applies to Bots:   %s\nApplies to Humans: %s```",
                config.getRoleID(),
                config.isAvailableForBots() ? TRUE_EMOTE : FALSE_EMOTE,
                config.isAvailableForHumans() ? TRUE_EMOTE : FALSE_EMOTE);
    }

    private void removeAutorole(CommandContext context, List<String> args) {
        if (args.get(1).equals("all")) {
            autoroleService.removeAllGuildConfigs(serverService.getServer(context.getGuild().getId()).getId());
            context.reply("**Cleared all Autorole configs!**");
        } else {
            String roleID = ParsingUtils.filterSnowflake(args.get(1));
            if (context.getGuild().getRoleById(roleID) == null)
                context.reply("**You did not specify a valid role ID!**");
            else if (autoroleService.getAutoroleConfigByRoleID(roleID) == null)
                context.reply("**This role is not set up for automation!**");
            else
                removeAutoroleConfig(roleID, context);
        }
    }

    private void removeAutoroleConfig(String roleID, CommandContext context) {
        autoroleService.deleteAutoroleConfig(roleID);
        context.reply("**Autorole config deleted!**");
    }

    private void addNewRole(CommandContext context) {
        List<AutoroleConfig> configs = autoroleService.getAutoroleConfigs(serverService.getServer(context.getGuild().getId()).getId());
        if (configs.size() == 10)
            context.reply("**You have 10 roles set up already! You cannot add more!**");
        else promptForRole(context);
    }

    private void promptForRole(CommandContext context) {
        getAwaitedReply(context, roleInput -> {
            String roleID = ParsingUtils.filterSnowflake(roleInput);
            if (context.getGuild().getRoleById(roleID) != null) promptForHumanAvailability(context, roleID);
            else context.getChannel().sendMessage("**You did not provide a valid role!**").queue();
        });
    }

    private void promptForHumanAvailability(CommandContext context, String roleID) {
        getAwaitedButton(context, "**Should this role be applied to humans?**", createTrueFalseButtons(), event -> {
            switch (event.getComponentId()) {
                case "YES":
                    promptForBotAvailability(context, roleID, true);
                    break;
                case "NO":
                    promptForBotAvailability(context, roleID, false);
                    break;
                default:
                    context.getChannel().sendMessage("**An error occurred. Our apologies.**").queue();
            }
        });
    }

    private List<ItemComponent> createTrueFalseButtons() {
        List<ItemComponent> components = new ArrayList<>();
        components.add(Button.secondary("YES", TRUE_EMOTE));
        components.add(Button.secondary("NO", FALSE_EMOTE));
        return components;
    }

    private void promptForBotAvailability(CommandContext context, String roleID, boolean applyToHumans) {
        getAwaitedButton(context, "**Should this role be applied to bots?**", createTrueFalseButtons(), event -> {
            switch (event.getComponentId()) {
                case "YES":
                    finishAddingAutorole(context, roleID, applyToHumans, true);
                    break;
                case "NO":
                    finishAddingAutorole(context, roleID, applyToHumans, false);
                    break;
                default:
                    context.getChannel().sendMessage("**An error occurred. Our apologies.**").queue();
            }
        });
    }

    private void finishAddingAutorole(CommandContext context, String roleID, boolean applyToHumans, boolean applyToBots) {
        if (!applyToHumans && !applyToBots)
            context.getChannel().sendMessage("**Autoroles must be added to at least either bots or humans!**").queue();
        else {
            long serverID = serverService.getServer(context.getGuild().getId()).getId();
            AutoroleConfig config = new AutoroleConfig(serverID, roleID, applyToBots, applyToHumans);
            context.getChannel().sendMessageEmbeds(createAutoroleSavedEmbed(config)).queue();
            autoroleService.saveAutoroleConfig(config);
        }
    }

    private MessageEmbed createAutoroleSavedEmbed(AutoroleConfig config) {
        EmbedBuilder autoroleSavedEmbedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Autorole Saved!")
                .setDescription(addAutoroleField(config));
        return autoroleSavedEmbedBuilder.build();
    }

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (!context.getMember().hasPermission(Permission.MANAGE_ROLES))
            context.getChannel().sendMessage("**You need to have the** `Manage Roles` **permission to use this command!**").queue();
        else {
            switch (args.get(0)) {
                case "add":
                    addNewRole(context);
                    break;
                case "remove":
                    removeAutorole(context, args);
                    break;
                case "list":
                    showAutoroles(context);
                    break;
                default:
                    context.reply("**Did you mean one of these?**\n" + this.getUsage());
                    break;
            }
            EnumSet<Permission> perms = context.getGuild().getSelfMember().getPermissions();
            if (!perms.contains(Permission.MANAGE_ROLES)) {
                context.reply("**NOTE: I do not have the** `Manage Roles` **permission. I need this to add roles to people!**");
            }
        }
    }

    @Override
    public String getDescription() {
        return "This command allows you to automatically assign roles to members and bots! You may choose if a role will be given to humans or bots. Maximum of 10 roles.";
    }

    @Override
    public String getUsage() {
        return "autorole add (then follow the instructions you are given)\n"
                + "autorole remove [role mention/ID/'all']\n"
                + "autorole list";
    }

    @Override
    public String getName() {
        return "autorole";
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
        return new String[]{"ar"};
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