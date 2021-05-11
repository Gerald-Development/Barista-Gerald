package main.java.de.voidtech.gerald.commands.actions;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class SlapCommand extends ActionsCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		super.sendAction(message, "slap");
	}

	@Override
	public String getDescription() {
		return "Slap a user!";
	}

	@Override
	public String getUsage() {
		return "slap @user";
	}

	@Override
	public String getName() {
		return "slap";
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
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {};
		return aliases;
	}

}
