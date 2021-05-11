package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class PPCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		
		long seed = Long.valueOf(message.getAuthor().getId());
		
		int ppSizeNumber = new Random(seed).nextInt(12);
		String phrase = getPhrase(ppSizeNumber);
		Color color = getColor(ppSizeNumber);
		String ppSize = String.valueOf(ppSizeNumber);		
		
		//It's best if nobody questions this
		if (message.getAuthor().getId().equals("341300268660555778")) {
			ppSize = "YEEEEEEEEEEEEEEEEEEEEEEESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS";
			phrase = "G a r g a n t u a n.";
			color = Color.magenta;
		}
		//You saw nothing...
		
		MessageEmbed ppSizeEmbed = new EmbedBuilder()//
				.setTitle("How big is your PP?")
				.setColor(color)
				.setDescription("Your PP is **" + ppSize + (ppSizeNumber == 1 ? " inch.** " : " inches.** ") + phrase)
				.build();
		
		message.getChannel().sendMessage(ppSizeEmbed).queue();
	}
	
	private Color getColor(int ppSize)
	{
		return ppSize > 8 
				? Color.GREEN 
				: ppSize > 4 
				? Color.ORANGE 
				: Color.RED;
	}
	
	private String getPhrase(int ppSize)
	{
		return ppSize > 8
				? "Thats pretty spankin' huge" 
				: ppSize > 4
				? "Meh could be bigger" 
				: "Does it even exist?";
	}
	
	@Override
	public String getDescription() {
		return "See how giant (or tiny) your pp is";
	}

	@Override
	public String getUsage() {
		return "pp";
	}

	@Override
	public String getName() {
		return "pp";
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
		String[] aliases = {};
		return aliases;
	}

}
