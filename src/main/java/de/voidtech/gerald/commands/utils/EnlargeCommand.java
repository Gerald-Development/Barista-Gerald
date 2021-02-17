package main.java.de.voidtech.gerald.commands.utils;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

@Command
public class EnlargeCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		String emoteText = args.get(0);
		String regexPattern = "([^0-9])";
		String emoteID = emoteText.replaceAll(regexPattern, "");
		
		
		
		Emote emote = null;
		emote = message.getJDA().getEmoteCache().getElementById(emoteID);
		
		if (emote == null) {
			message.getChannel().sendMessage("**Could not find that emote**").queue();
		} else {
			String URL = emote.getImageUrl();
			message.getChannel().sendMessage(URL).queue();
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to send an enlarged image of an emote";
	}

	@Override
	public String getUsage() {
		return "enlarge [emote id/:emote:]";
	}

	@Override
	public String getName() {
		return "enlarge";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILS;
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
