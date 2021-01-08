package main.java.de.voidtech.gerald.commands.fun;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;

public class AskCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args)
	{
		if(args.size() > 0)
		{
			Random random = new Random();
			List<String> answers = getAnswers();
			message.getChannel().sendMessage(answers.get(random.nextInt(answers.size()))).queue();
		}
	}
	
	private List<String> getAnswers()
	{
		List<String> answers = new ArrayList<>();
		
		answers.add("Yes");
		answers.add("No");
		answers.add("Not a chance");
		answers.add("Absolutely");
		answers.add("Absolutely not");
		answers.add("Without a shadow of a doubt");
		answers.add("You wish");
		answers.add("Maybe not this time");
		answers.add("That sounds like a great idea");
		answers.add("Are you a moron? of course not");
		answers.add("A thousand times yes");
		
		return answers;
	}

	@Override
	public String getDescription() {
		return "ask Gerald something and he will answer";
	}

	@Override
	public String getUsage() {
		return "ask are animals with guns cool?";
	}

}
