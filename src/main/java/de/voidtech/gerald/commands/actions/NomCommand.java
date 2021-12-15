package main.java.de.voidtech.gerald.commands.actions;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;

@Command
public class NomCommand extends ActionsCommand {

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		super.sendAction(context, ActionType.NOM);
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
		return new String[]{"bite"};
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
