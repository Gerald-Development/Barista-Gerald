package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;
import java.util.Random;

@Command
public class PPCommand extends AbstractCommand{

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		
		long seed = Long.parseLong(context.getAuthor().getId());
		
		int ppSizeNumber = new Random(seed).nextInt(12);
		String phrase = getPhrase(ppSizeNumber);
		Color color = getColor(ppSizeNumber);
		String ppSize = String.valueOf(ppSizeNumber);		
		
		//It's best if nobody questions this
		if (context.getAuthor().getId().equals("341300268660555778")) {
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
		
		context.reply(ppSizeEmbed);
	}
	
	private Color getColor(int ppSize)
	{
		return ppSize > 6 
				? Color.GREEN 
				: ppSize > 4 
				? Color.ORANGE 
				: Color.RED;
	}
	
	private String getPhrase(int ppSize)
	{
		return ppSize > 6
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
		return new String[]{"ppsize"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
	@Override
	public boolean isSlashCompatible() {
		return true;
	}

}
