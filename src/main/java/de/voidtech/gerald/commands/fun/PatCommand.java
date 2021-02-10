package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.ActionsCommand;
import net.dv8tion.jda.api.entities.Message;

@Command
public class PatCommand extends ActionsCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		super.sendAction(message, "pat");
	}

	@Override
	public String getDescription() {
		return "Pat a user!";
	}

	@Override
	public String getUsage() {
		return "pat @user";
	}

	@Override
	public String getName() {
		return "pat";
	}
}
