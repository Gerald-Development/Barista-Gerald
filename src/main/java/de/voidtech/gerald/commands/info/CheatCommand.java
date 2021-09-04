package main.java.de.voidtech.gerald.commands.info;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jdautilities.menu.Paginator.Builder;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command
public class CheatCommand extends AbstractCommand {

	private static final String CHEAT_SH_URL = "https://cheat.sh/";
	private static final Logger LOGGER = Logger.getLogger(CheatCommand.class.getName());
	
	@Autowired
	private EventWaiter waiter;
	
	private String getCheatSheet(String topic) {		
		try {
			String requestURL = CHEAT_SH_URL + topic + "?TQ";
			Document doc = Jsoup.connect(requestURL).get();
			return doc.select("pre").first().text();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return "";
	}
	
	private String[] generatePages(String cheatSheet) {
		if (cheatSheet.startsWith("Unknown topic.")) {
            return new String[]{"```\n" + cheatSheet + "```"};
			
		} else {
			List<String> iterator = new ArrayList<String>(Arrays.asList(cheatSheet.split("\n")));		
			String responseString = "```py\n";
			List<String> response = new ArrayList<String>();
			
			for (int i = 0; i < iterator.size(); i++) {
				if (responseString.length() + iterator.get(i).length() + 3 < 2000) {
					responseString += iterator.get(i) + "\n";
				} else {
					response.add(responseString + "```");
					responseString = "```py\n";
				}
			}
			if (response.size() == 0) {
				response.add(responseString + "```");
			}
			return response.toArray(new String[0]);
		}
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		context.getChannel().sendMessage("**Searching...**").queue(botMessage -> {
			String topic = String.join(" ", args);
			String cheatSheet = getCheatSheet(topic);
			
			Builder pagedResponseBuilder = new Paginator.Builder();
			pagedResponseBuilder.setColor(Color.ORANGE);
			pagedResponseBuilder.showPageNumbers(true);
			pagedResponseBuilder.setEventWaiter(waiter);
			pagedResponseBuilder.addItems(generatePages(cheatSheet));
			pagedResponseBuilder.setItemsPerPage(1);
			pagedResponseBuilder.waitOnSinglePage(true);
			pagedResponseBuilder.setTimeout(120, TimeUnit.SECONDS);
			pagedResponseBuilder.setText("**Your cheat sheet:**");
			pagedResponseBuilder.setFinalAction(msg -> {
                    msg.clearReactions().queue();
            });
			Paginator pagedResponseEmbed = pagedResponseBuilder.build();
			pagedResponseEmbed.display(botMessage);
		});
	}

	@Override
	public String getDescription() {
		return "Allows you to get a programming-related cheatsheet from cheat.sh!";
	}

	@Override
	public String getUsage() {
		return "cheat java\n"
			 + "cheat java/:learn\n"
			 + "cheat python; reverse a linked list";
	}

	@Override
	public String getName() {
		return "cheat";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.INFO;
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
        return new String[]{"cheatsheet", "cs"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}