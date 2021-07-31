package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.service.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class HelpCommand extends AbstractCommand{	
	
	@Autowired
	private List<AbstractCommand> commands;
	
	@Autowired
	private MessageHandler msgHandler;
 
	private static final String TRUE_EMOTE = "\u2705";
	private static final String FALSE_EMOTE = "\u274C";
	
	private String capitaliseFirstLetter(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}
	
	private void showCategoryList(Message message) {
		
		boolean inlineFieldState = true;
		
		EmbedBuilder categoryListEmbedBuilder = new EmbedBuilder();
		categoryListEmbedBuilder.setColor(Color.ORANGE);
		categoryListEmbedBuilder.setTitle("Barista Gerald Help", GlobalConstants.LINKTREE_URL);
		categoryListEmbedBuilder.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl());
		categoryListEmbedBuilder.setFooter("Barista Gerald Version " + GlobalConstants.VERSION, message.getJDA().getSelfUser().getAvatarUrl());
		
		for (CommandCategory commandCategory : CommandCategory.values()) {
			String title = capitaliseFirstLetter(commandCategory.getCategory()) + " " + commandCategory.getIcon();
			String description = "```\nhelp " + commandCategory.getCategory() + "\n```";
			categoryListEmbedBuilder.addField(title, description, inlineFieldState);
			inlineFieldState = !inlineFieldState;
		}
		
		categoryListEmbedBuilder.addField("Any Command :clipboard: ", "```\nhelp [command]\n```", true);
		
		MessageEmbed categoryListEmbed = categoryListEmbedBuilder.build();
		
		message.getChannel().sendMessageEmbeds(categoryListEmbed).queue();
	}
	
	private boolean isCommandCategory(String categoryName) {
		for (CommandCategory commandCategory : CommandCategory.values()) {
			if (commandCategory.getCategory().equals(categoryName))
				return true;
		}
		return false;
	};
	
	
	private String getCategoryIconByName(String name) {
		for (CommandCategory commandCategory : CommandCategory.values()) {
			if (commandCategory.getCategory().equals(name))
				return commandCategory.getIcon();
		}
		return "";
	}
	
	private boolean isCommand(String commandName) {
		String commandToBeFound = commandName;
		if (msgHandler.aliases.containsKey(commandToBeFound)) {
			return true;
		} else {
			for (AbstractCommand command : commands)
				if(command.getName().equals(commandToBeFound)) {
					return true;
			}	
		}
		return false;
	};
	
	private void showCommandCategory(Message message, String categoryName) {
		String commandList = "";
		for (AbstractCommand command : commands) {
			if(command.getCommandCategory().getCategory().equals(categoryName))
				commandList = commandList + "`" + command.getName()  + "`, ";
		}
		commandList = commandList.substring(0, commandList.length() - 2);
		
		MessageEmbed commandHelpEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle(capitaliseFirstLetter(categoryName) + " Commands Help", GlobalConstants.LINKTREE_URL)
				.addField(capitaliseFirstLetter(categoryName) + " Commands " + getCategoryIconByName(categoryName), commandList, false)
				.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
				.setFooter("Barista Gerald Version " + GlobalConstants.VERSION, message.getJDA().getSelfUser().getAvatarUrl())
				.build();
		message.getChannel().sendMessageEmbeds(commandHelpEmbed).queue();
		
	};
	
	//TODO: Those two showCommandCategory methods dont have the same purpose at all but they are named the same way. 
	//TODO: Also this should be a method in the CommandCategory enum.
	private String showCommandCategory(CommandCategory category) {
		return category == null ? "No Category" : capitaliseFirstLetter(category.getCategory());
	}
	
	private AbstractCommand getCommand(String name) {
		String commandToBeFound = name;
		if (msgHandler.aliases.containsKey(name))
			commandToBeFound = msgHandler.aliases.get(name);
		for (AbstractCommand command : commands) {
			if(command.getName().equals(commandToBeFound))
				return command;
		}
		return null;
	}
	
	private void showCommand(Message message, String commandName) {
		AbstractCommand commandToBeDisplayed = getCommand(commandName);	

		MessageEmbed commandHelpEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("How it works: " + capitaliseFirstLetter(commandToBeDisplayed.getName()) + " Command", GlobalConstants.LINKTREE_URL)
				.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
				.addField("Command Name", "```" + capitaliseFirstLetter(commandToBeDisplayed.getName()) + "```", true)
				.addField("Category", "```" + showCommandCategory(commandToBeDisplayed.getCommandCategory()) + "```", true)
				.addField("Description", "```" + commandToBeDisplayed.getDescription() + "```", false)
				.addField("Usage", "```" + commandToBeDisplayed.getUsage() + "```", false)
				.addField("Requires Arguments", "```" + showBooleanEmote(commandToBeDisplayed.requiresArguments()) + "```", true)
				.addField("Is DM Capable", "```" + showBooleanEmote(commandToBeDisplayed.isDMCapable()) + "```", true)
				.addField("Command Aliases", "```" + showCommandAliases(commandToBeDisplayed.getCommandAliases()) + "```", false)
				.setFooter("Barista Gerald Version " + GlobalConstants.VERSION, message.getJDA().getSelfUser().getAvatarUrl())
				.build();
		message.getChannel().sendMessageEmbeds(commandHelpEmbed).queue();
	}
	

	private String showCommandAliases(String[] aliases) {
		return aliases == null ? "No aliases" : String.join(", ", aliases);
	}

	private String showBooleanEmote(boolean option) {
		return option ? TRUE_EMOTE : FALSE_EMOTE;
	}

	@Override
	public void executeInternal(Message message, List<String> args) {
		if (!commands.contains(this)) commands.add(this);	
		
		if (args.size() == 0)
			showCategoryList(message);
		else {
			String itemToBeQueried = args.get(0).toLowerCase();
			if (isCommandCategory(itemToBeQueried)) showCommandCategory(message, itemToBeQueried);
			else if (isCommand(itemToBeQueried)) showCommand(message, itemToBeQueried);
			else message.getChannel().sendMessage("**That command/category could not be found!**").queue();
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
		String[] aliases = {"commands", "h"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return false;
	}

}