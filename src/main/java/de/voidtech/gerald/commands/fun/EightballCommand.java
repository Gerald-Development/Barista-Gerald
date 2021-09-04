package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;
import java.util.Random;

@Command
public class EightballCommand extends AbstractCommand{

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		context.getChannel().sendMessage(new Random().nextBoolean() ? "Yes" : "No").queue();
	}

	@Override
	public String getDescription() {
		return "Answers yes or no to any question";
	}

	@Override
	public String getUsage() {
		return "8ball is this a fantastic question?";
	}

	@Override
	public String getName() {
		return "8ball";
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
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"eightball", "8b"};
		return aliases;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
}
