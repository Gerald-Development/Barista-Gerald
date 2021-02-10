package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.ActionsCommand;
import net.dv8tion.jda.api.entities.Message;

@Command
public class NomCommand extends ActionsCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		super.sendAction(message, "nom");
	}

	@Override
	public String getDescription() {
		return "Nom a user!";
	}

	@Override
	public String getUsage() {
		return "nom @user";
	}

	@Override
	public String getName() {
		return "nom";
	}
	
}
