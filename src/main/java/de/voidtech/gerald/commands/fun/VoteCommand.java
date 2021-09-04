package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;

@Command
public class VoteCommand extends AbstractCommand{
	private final static String CHECK = "U+2705";
	private final static String CROSS = "U+274E";
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		//TODO (from: Franziska): Should this be available via SlashCommands? Alternative could be to just send a message with the thing.
		//context.addReaction(CHECK).queue();
		//context.addReaction(CROSS).queue();
		context.reply("Not implemented due to implementation of SlashCommands. Please ask the developers to reimplement this.");
	}

	@Override
	public String getDescription() {
		return "Allows you to hold a quick vote on your very sensitive topics";
	}

	@Override
	public String getUsage() {
		return "vote is this democracy?";
	}

	@Override
	public String getName() {
		return "vote";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
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
		return new String[]{"poll"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}