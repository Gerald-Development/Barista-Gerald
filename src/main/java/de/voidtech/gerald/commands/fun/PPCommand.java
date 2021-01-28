package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PPCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {

		int ppSizeNumber = new Random().nextInt(12);
		String phrase = getPhrase(ppSizeNumber);
		Color color = getColor(ppSizeNumber);
		
		MessageEmbed ppSizeEmbed = new EmbedBuilder()//
				.setTitle("How big is your PP?")
				.setColor(color)
				.setDescription("Your PP is **" + ppSizeNumber + (ppSizeNumber == 1 ? " inch.** " : " inches.** ") + phrase)
				.build();
		
		message.getChannel().sendMessage(ppSizeEmbed).queue();
	}
	
	private Color getColor(int ppSize)
	{
		return ppSize > 10 
				? Color.GREEN 
				: ppSize > 6 
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

}
