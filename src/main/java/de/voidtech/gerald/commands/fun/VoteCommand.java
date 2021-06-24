package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class VoteCommand extends AbstractCommand{
	private final static String CHECK = "U+2705";
	private final static String CROSS = "U+274E";
	@Override
	public void executeInternal(Message message, List<String> args) {
		message.addReaction(CHECK).queue();
		message.addReaction(CROSS).queue();
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
		String[] aliases = {"poll"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}