package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.List;
import java.util.Random;

@Command
public class ShipCommand extends AbstractCommand{
	
	private final static String HEART = " :heart: ";
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		
		if (context.getMentionedMembers().size() < 2) context.getChannel().sendMessage("**You must mention 2 people to ship!**").queue();
		else {
			User user1 = context.getMentionedMembers().get(0).getUser();
			User user2 = context.getMentionedMembers().get(1).getUser();
			int shipRating = new Random(user1.getIdLong() - user2.getIdLong()).nextInt(101);
			String phrase = getPhrase(shipRating);
			Color color = getColor(shipRating);
			
			MessageEmbed shipEmbed = new EmbedBuilder()//
					.setTitle(user1.getAsTag() + HEART + user2.getAsTag())
					.setColor(color)
					.setDescription(String.format("Your love match percentage is %d%c %s", shipRating, '%', phrase))
					.build();
			
			context.getChannel().sendMessageEmbeds(shipEmbed).queue();
		}
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
				? "You're made for each other :D"
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
		return "ship @member @member";
	}

	@Override
	public String getName() {
		return "ship";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
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
		return new String[]{"stan"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
