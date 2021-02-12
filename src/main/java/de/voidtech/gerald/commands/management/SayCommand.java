package main.java.de.voidtech.gerald.commands.management;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@Command
public class SayCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		if(message.getMember().hasPermission(Permission.MESSAGE_MANAGE))
		{
			String msg = String.join(" ", args);
			message.getChannel().sendMessage(msg).queue(response -> {
				message.delete().queue();
			});		
		}
	}

	@Override
	public String getDescription() {
		return "repeats the message you type";
	}

	@Override
	public String getUsage() {
		return "say a very exciting message";
	}

	@Override
	public String getName() {
		return "say";
	}
	
	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.MANAGEMENT;
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
