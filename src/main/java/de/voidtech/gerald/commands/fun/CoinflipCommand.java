package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;

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

}
