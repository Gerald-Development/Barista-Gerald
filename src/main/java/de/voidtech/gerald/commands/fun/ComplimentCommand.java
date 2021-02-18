package main.java.de.voidtech.gerald.commands.fun;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class ComplimentCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		List<String> compliments = getCompliments();
		message.getChannel().sendMessage(compliments.get(new Random().nextInt(compliments.size()))).queue();
	}
	
	private List<String> getCompliments()
	{
		List<String> complimentList = new ArrayList<>();
		complimentList.add("I'm proud of what you've achieved today, even if it was just getting out of bed :)");
		complimentList.add("you bring out the best in people :)");
		complimentList.add("you always make my day better :)");
		complimentList.add("your smile is like sunshine on a cloudy day :)");
		complimentList.add("I could talk to you for hours and still be left wanting more :)");
		complimentList.add("your eyes are so warm and welcoming :)");
		complimentList.add("your smile is infectious :)");
		complimentList.add("I'm proud of how far you've come :)");
		complimentList.add("Don't let anyone get you down, you are a powerful and amazing person :)");
		complimentList.add( "I've never met someone like you before :)");
		
		return complimentList;
	}

	@Override
	public String getDescription() {
		return "Receive a heartwarming compliment";
	}

	@Override
	public String getUsage() {
		return "compliment";
	}

	@Override
	public String getName() {
		return "compliment";
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

}
