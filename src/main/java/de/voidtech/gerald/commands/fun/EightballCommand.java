package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;

public class EightballCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		message.getChannel().sendMessage(new Random().nextBoolean() ? "Yes" : "No").queue();		
	}

	@Override
	public String getDescription() {
		return "Answers yes or no to any question";
	}

	@Override
	public String getUsage() {
		return "8ball is this a fantastic question?";
	}

}
