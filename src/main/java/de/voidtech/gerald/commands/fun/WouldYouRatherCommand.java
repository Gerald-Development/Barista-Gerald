package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command
public class WouldYouRatherCommand extends AbstractCommand {
	private static final Logger LOGGER = Logger.getLogger(WouldYouRatherCommand.class.getName());

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		context.getChannel().sendTyping().queue();
		try {
			Document doc = Jsoup.connect("https://either.io/").get();
			String answerA = doc.select("div.result.result-1 > .option-text").first().text();
			String answerB = doc.select("div.result.result-2 > .option-text").first().text();
			
			context.getChannel().sendMessage("**Would You Rather:**\n:a:" + answerA + "\n**OR:**\n:b:" + answerB).queue(sentMessage -> {
				sentMessage.addReaction("🅰").queue();
				sentMessage.addReaction("🅱").queue();
			});
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}		
	}

	@Override
	public String getDescription() {
		return "Prompts you with a 'Would You Rather' question";
	}

	@Override
	public String getUsage() {
		return "wyr";
	}

	@Override
	public String getName() {
		return "wyr";
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
		return false;
	}
	
	@Override
	public String[] getCommandAliases() {
		return new String[]{"wouldyourather"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
