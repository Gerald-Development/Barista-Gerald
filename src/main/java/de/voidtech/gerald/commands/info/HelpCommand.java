package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class HelpCommand extends AbstractCommand{	
	 @Autowired
	 private List<AbstractCommand> commands;
	
	private String capitaliseFirstLetter(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}
	
	private void showCategoryList(Message message) {		
		String commandCategoryList = "";
		
		for (CommandCategory commandCategory : CommandCategory.values()) {
			commandCategoryList = commandCategoryList + "```\n► " + capitaliseFirstLetter(commandCategory.getCategory()) + "```\n"; 
		}
		
		MessageEmbed categoryListEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Barista Gerald Help", GlobalConstants.LINKTREE_URL)
				.setDescription("**The command categories are as follows:** \n\n" + commandCategoryList + "\nUse `help [commandName]` or `help [categoryName]` For more information!")
				.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
				.setFooter("Barista Gerald Version " + GlobalConstants.VERSION, message.getJDA().getSelfUser().getAvatarUrl())
				.build();
		message.getChannel().sendMessage(categoryListEmbed).queue();
	}
	
	private boolean isCommandCategory(String categoryName) {
		for (CommandCategory commandCategory : CommandCategory.values()) {
			if (commandCategory.getCategory().equals(categoryName)) {
				return true;
			}
		}
		return false;
	};
	
	private boolean isCommand(String commandName) {
		for (AbstractCommand command : commands) {
			if(command.getName().equals(commandName)) {
				return true;
			}
		}
		return false;
	};
	
	private void showCommandCategory(Message message, String categoryName) {
		String commandList = "";
		for (AbstractCommand command : commands) {
			if(command.getCommandCategory().getCategory().equals(categoryName)) {
				commandList = commandList + "`" + command.getName()  + "`, ";
			}
		}
		commandList = commandList.substring(0, commandList.length() - 2);
		
		MessageEmbed commandHelpEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Barista Gerald Help", GlobalConstants.LINKTREE_URL)
				.addField(capitaliseFirstLetter(categoryName) + " Commands", commandList, false)
				.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
				.setFooter("Barista Gerald Version " + GlobalConstants.VERSION, message.getJDA().getSelfUser().getAvatarUrl())
				.build();
		message.getChannel().sendMessage(commandHelpEmbed).queue();
		
	};
	
	private void showCommand(Message message, String commandName) {
		AbstractCommand commandToBeDisplayed = null;		
		for (AbstractCommand command : commands) {
			if(command.getName().equals(commandName)) {
				commandToBeDisplayed = command;
			}
		}
		MessageEmbed commandHelpEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Barista Gerald Help", GlobalConstants.LINKTREE_URL)
				.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
				.addField("Command Name", "```" + capitaliseFirstLetter(commandToBeDisplayed.getName()) + "```", false)
				.addField("Description", "```" + commandToBeDisplayed.getDescription() + "```", false)
				.addField("Usage", "```" + commandToBeDisplayed.getUsage() + "```", false)
				.addField("Requires Arguments", "```" + commandToBeDisplayed.requiresArguments() + "```", true)
				.addField("Is DM Capable", "```" + commandToBeDisplayed.isDMCapable() + "```", true)
				.addField("Category", "```" + capitaliseFirstLetter(commandToBeDisplayed.getCommandCategory().getCategory()) + "```", true)
				.setFooter("Barista Gerald Version " + GlobalConstants.VERSION, message.getJDA().getSelfUser().getAvatarUrl())
				.build();
		message.getChannel().sendMessage(commandHelpEmbed).queue();
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (args.size() == 0) {
			showCategoryList(message);
		} else {
			
			String itemToBeQueried = args.get(0).toLowerCase();
			
			if (isCommandCategory(itemToBeQueried)) {
				showCommandCategory(message, itemToBeQueried);
			} else if (isCommand(itemToBeQueried)) {
				showCommand(message, itemToBeQueried);
			} else {
				message.getChannel().sendMessage("**That command/category could not be found!**").queue();
			}
		}
		
	}

	@Override
	public String getDescription() {
		return "Shows you all of Barista Gerald's commands and how to use them";
	}

	@Override
	public String getUsage() {
		return "help OR help [name of command] OR help [name of category]";
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

}