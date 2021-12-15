package main.java.de.voidtech.gerald.commands.actions;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;

@Command
public class HugCommand extends ActionsCommand {

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		super.sendAction(context, ActionType.HUG);
	}

	@Override
	public String getDescription() {
		return "Hug a user!";
	}

	@Override
	public String getUsage() {
		return "hug @user";
	}

	@Override
	public String getName() {
		return "hug";
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
		return new String[]{"hold"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
	@Override
	public boolean isSlashCompatible() {
		return true;
	}

}
