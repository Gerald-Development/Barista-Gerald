package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;
import java.util.Random;

@Command
public class CoinflipCommand extends AbstractCommand{

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		context.reply(new Random().nextBoolean() ? "Heads wins!" : "Tails wins!");
	}

	@Override
	public String getDescription() {
		return "flips a coin";
	}

	@Override
	public String getUsage() {
		return "coinflip";
	}

	@Override
	public String getName() {
		return "coinflip";
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
		String[] aliases = {"coin", "flip"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
