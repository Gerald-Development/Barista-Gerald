package main.java.de.voidtech.gerald.commands;

import java.util.List;

@Deprecated //COMMAND FOR JUNIT TESTS ONLY!!
public class TestCommand extends AbstractCommand{

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		context.getAuthor();
	}


	@Override
	public String getDescription() {
		return "not your business";
	}

	@Override
	public String getUsage() {
		return "";
	}


	@Override
	public String getName() {
		return "test";
	}


	@Override
	public CommandCategory getCommandCategory() {
		return null;
	}


	@Override
	public boolean isDMCapable() {
		return false;
	}


	@Override
	public boolean requiresArguments() {
		return false;
	}
	
	@Override
	public String[] getCommandAliases() {
		return new String[]{"test"};
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
