package main.java.de.voidtech.gerald.commands.effects;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;

@Command
public class ClapCommand extends AbstractCommand {

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String finalMessage = ":clap: " + String.join(" :clap: ", args) + " :clap:";
		context.reply(finalMessage);
	}

	@Override
	public String getDescription() {
		return ":clap: claps :clap: your :clap: messages :clap: ";
	}

	@Override
	public String getUsage() {
		return "clap a very normal message";
	}

	@Override
	public String getName() {
		return "clap";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.EFFECTS;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		return new String[]{"clapback"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}
