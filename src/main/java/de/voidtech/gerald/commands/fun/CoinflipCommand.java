package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class CoinflipCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		message.getChannel().sendMessage(new Random().nextBoolean() ? "Heads wins!" : "Tails wins!").queue();
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

}
