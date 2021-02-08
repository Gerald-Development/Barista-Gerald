package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class ShipCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		
		if(args.size() != 2) message.getChannel().sendMessage("You must supply two names!").queue();
		
		String name1 = message.getMentionedMembers().size() > 1 
				? message.getMentionedMembers().get(0).getUser().getAsTag()
				: args.get(0);
		
		String name2 = message.getMentionedMembers().size() >= 2 
				? message.getMentionedMembers().get(1).getUser().getAsTag()
				: args.get(1);
		
		int shipRating = new Random().nextInt(101);
		String phrase = getPhrase(shipRating);
		Color color = getColor(shipRating);
		
		MessageEmbed shipEmbed = new EmbedBuilder()//
				.setTitle(name1 + " ❤ " + name2)
				.setColor(color)
				.setDescription(String.format("Your love match percentage is %d%c %s", shipRating, '%', phrase))
				.build();
		
		message.getChannel().sendMessage(shipEmbed).queue();
	}
	
	private Color getColor(int shipRating)
	{
		return shipRating > 70 
				? Color.GREEN 
				: shipRating > 30 
				? Color.ORANGE 
				: Color.RED;
	}
	
	private String getPhrase(int shipRating)
	{
		return shipRating > 70 
				? "You're made for eachother :D" 
				: shipRating > 50 
				? "This could work out :)" 
				: "Maybe it's not meant to be :(";
	}
	
	@Override
	public String getDescription() {
		return "See if two people are meant to be together!";
	}

	@Override
	public String getUsage() {
		return "ship ";
	}

	@Override
	public String getName() {
		return "ship";
	}

}
