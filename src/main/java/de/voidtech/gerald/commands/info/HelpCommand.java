package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.CommandService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Command
public class HelpCommand extends AbstractCommand {

    private static final String TRUE_EMOTE = "\u2705";
    private static final String FALSE_EMOTE = "\u274C";

    @Autowired
    private List<AbstractCommand> commandsList;
    @Autowired
    private CommandService commandService;

    private String capitaliseFirstLetter(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private void showCategoryList(CommandContext command) {
        EmbedBuilder categoryListEmbedBuilder = new EmbedBuilder();
        categoryListEmbedBuilder.setColor(Color.ORANGE);
        categoryListEmbedBuilder.setTitle("Barista Gerald Help", GlobalConstants.LINKTREE_URL);
        categoryListEmbedBuilder.setThumbnail(command.getJDA().getSelfUser().getAvatarUrl());
        categoryListEmbedBuilder.setFooter("Barista Gerald Version " + GlobalConstants.VERSION, command.getJDA().getSelfUser().getAvatarUrl());

        for (CommandCategory commandCategory : CommandCategory.values()) {
            if (commandCategory.equals(CommandCategory.INVISIBLE) && !command.isMaster()) continue;
            String title = capitaliseFirstLetter(commandCategory.getCategory()) + " " + commandCategory.getIcon();
            String description = "```\n" + commandCategory.getDescription() + "\n```";
            categoryListEmbedBuilder.addField(title, description, true);
        }

        categoryListEmbedBuilder.addField("Any Command :clipboard: ", "```\nhelp [command]\n```", true);
        command.reply(categoryListEmbedBuilder.build());
    }

    private CommandCategory getCategoryByName(String name) {
        return Arrays.stream(CommandCategory.values()).filter(c -> c.getCategory().equals(name))
                .findFirst()
                .orElse(null);
    }

    private boolean isCommandCategory(String categoryName, boolean isMaster) {
        CommandCategory cat = getCategoryByName(categoryName); //Meow
        if (cat == null) return false;
        else {
            if (cat.equals(CommandCategory.INVISIBLE) && !isMaster) return false;
            else return true;
        }
    }

    private String getCategoryIconByName(String name) {
        CommandCategory category = getCategoryByName(name);
        return category == null ? "" : category.getIcon();
    }

    private AbstractCommand getCommandFromNameOrAlias(String commandName) {
        Optional<AbstractCommand> nameMatch = commandsList.stream()
                .filter(c -> c.getName().equals(commandName))
                .findFirst();
        Optional<AbstractCommand> aliasMatch = commandsList.stream()
                .filter(c -> List.of(c.getCommandAliases()).contains(commandName))
                .findFirst();
        return aliasMatch.isPresent() ? aliasMatch.get() : nameMatch.isPresent() ? nameMatch.get() : null;
    }

    private boolean isCommand(String commandName, boolean isMaster) {
        AbstractCommand command = getCommandFromNameOrAlias(commandName);
        //Please god work
        if (command == null) return false;
        else {
            if (command.getCommandCategory().equals(CommandCategory.INVISIBLE) && !isMaster) return false;
            else return true;
        }
    }

    private void showCommandsFromCategory(CommandContext context, String categoryName) {
        StringBuilder commandListBuilder = new StringBuilder();
        for (AbstractCommand command : commandsList) {
            if (command.getCommandCategory().getCategory().equals(categoryName))
                commandListBuilder.append("`").append(command.getName()).append("`, ");
        }
        String commandList = commandListBuilder.toString();
        commandList = commandList.substring(0, commandList.length() - 2);

        MessageEmbed commandHelpEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(capitaliseFirstLetter(categoryName) + " Commands Help", GlobalConstants.LINKTREE_URL)
                .addField(capitaliseFirstLetter(categoryName) + " Commands " + getCategoryIconByName(categoryName), commandList, false)
                .setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
                .setFooter("Barista Gerald Version " + GlobalConstants.VERSION, context.getJDA().getSelfUser().getAvatarUrl())
                .build();
        context.reply(commandHelpEmbed);
    }

    private String displayCommandCategoryOrNull(CommandCategory category) {
        return category == null ? "No Category" : capitaliseFirstLetter(category.getCategory());
    }

    private void showCommand(CommandContext context, String commandName) {
        AbstractCommand commandToBeDisplayed = getCommandFromNameOrAlias(commandName);
        MessageEmbed commandHelpEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("How it works: " + capitaliseFirstLetter(Objects.requireNonNull(commandToBeDisplayed).getName()) + " Command", GlobalConstants.LINKTREE_URL)
                .setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
                .addField("Command Name", "```" + capitaliseFirstLetter(commandToBeDisplayed.getName()) + "```", true)
                .addField("Category", "```" + displayCommandCategoryOrNull(commandToBeDisplayed.getCommandCategory()) + "```", true)
                .addField("Description", "```" + commandToBeDisplayed.getDescription() + "```", false)
                .addField("Usage", "```" + commandToBeDisplayed.getUsage() + "```", false)
                .addField("Requires Arguments", "```" + booleanToEmote(commandToBeDisplayed.requiresArguments()) + "```", true)
                .addField("Is DM Capable", "```" + booleanToEmote(commandToBeDisplayed.isDMCapable()) + "```", true)
                .addField("Command Aliases", "```" + showCommandAliases(commandToBeDisplayed.getCommandAliases()) + "```", false)
                .setFooter("Barista Gerald Version " + GlobalConstants.VERSION, context.getJDA().getSelfUser().getAvatarUrl())
                .build();
        context.reply(commandHelpEmbed);
    }

    private String showCommandAliases(String[] aliases) {
        return aliases == null ? "No aliases" : String.join(", ", aliases);
    }

    private String booleanToEmote(boolean option) {
        return option ? TRUE_EMOTE : FALSE_EMOTE;
    }

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (!commandsList.contains(this)) commandsList.add(this);

        if (args.isEmpty()) showCategoryList(context);
        else {
            String itemToBeQueried = args.get(0).toLowerCase();
            if (isCommandCategory(itemToBeQueried, context.isMaster()))
                showCommandsFromCategory(context, itemToBeQueried);
            else if (isCommand(itemToBeQueried, context.isMaster())) showCommand(context, itemToBeQueried);
            else context.reply("**That command/category could not be found!**");
        }
    }

    @Override
    public String getDescription() {
        return "Shows you all of Barista Gerald's commands and how to use them";
    }

    @Override
    public String getUsage() {
        return "help\n"
                + "help [name of command]\n"
                + "help [name of category]";
    }

    @Override
    public String getName() {
        return "help";
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
        return new String[]{"commands", "h"};
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