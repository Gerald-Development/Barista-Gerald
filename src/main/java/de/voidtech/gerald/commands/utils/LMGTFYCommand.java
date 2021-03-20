package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class LMGTFYCommand extends AbstractCommand{
	
	private static final String YOUTUBE_URL = "https://www.youtube.com/results?search_query=";
	private static final String BING_URL = "https://www.bing.com/search?q=";
	private static final String DUCKDUCKGO_URL = "https://duckduckgo.com/?q=";
	private static final String ECOSIA_URL = "https://ecosia.org/search?q=";
	private static final String GOOGLE_URL = "https://www.google.com/search?q=";
	private static final String LMGTFY_URL = "https://lmgtfy.app/?q=";
	
	private HashMap<String, String> getEnginesAndNames() {
		
		HashMap<String, String> searchEngineMap = new HashMap<String, String>();
		searchEngineMap.put("YouTube Search", YOUTUBE_URL);
		searchEngineMap.put("Bing Search", BING_URL);
		searchEngineMap.put("DuckDuckGo Search", DUCKDUCKGO_URL);
		searchEngineMap.put("Ecosia Search", ECOSIA_URL);
		searchEngineMap.put("Google Search", GOOGLE_URL);
		searchEngineMap.put("LMGTFY Search", LMGTFY_URL);
		
		return searchEngineMap;
	}
	
	String embedMessage = "";
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		HashMap<String, String> enginesAndNames = getEnginesAndNames();
		
		for(String entry: enginesAndNames.keySet()) {
			String name = entry;
			String engine = enginesAndNames.get(name);
			String URL = engine + String.join("+", args);
			embedMessage += "**[" + name + "](" + URL + ")**\n";
		}
		
		MessageEmbed LMGTFYResultEmbed = new EmbedBuilder()
				.setColor(Color.CYAN)
				.setTitle("We searched far and wide. This is what we found:")
				.setDescription(embedMessage)
				.setFooter("You searched for '" + String.join(" ", args) + "'")
				.build();
		message.getChannel().sendMessage(LMGTFYResultEmbed).queue();
		
	}

	@Override
	public String getDescription() {
		return "Googles something for you! Powered by Ecosia (For the trees)";
	}

	@Override
	public String getUsage() {
		return "lmgtfy a wonderful thing";
	}

	@Override
	public String getName() {
		return "lmgtfy";
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
