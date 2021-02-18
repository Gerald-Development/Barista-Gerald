package main.java.de.voidtech.gerald.commands.actions;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class PokeCommand extends ActionsCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		super.sendAction(message, "poke");
	}

	@Override
	public String getDescription() {
		return "Poke a user!";
	}

	@Override
	public String getUsage() {
		return "poke @user";
	}

	@Override
	public String getName() {
		return "poke";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.ACTIONS;
	}

	@Override
	public boolean isDMCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
}
