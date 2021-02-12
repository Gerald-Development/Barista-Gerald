package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class ClapCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		String finalMessage = "ðŸ‘�" + String.join("ðŸ‘�", args) + "ðŸ‘�";
		message.getChannel().sendMessage(finalMessage).queue();
	}

	@Override
	public String getDescription() {
		return "ðŸ‘�doesðŸ‘�thisðŸ‘�toðŸ‘�yourðŸ‘�messagesðŸ‘�";
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
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
}
