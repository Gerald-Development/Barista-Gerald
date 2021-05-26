package main.java.de.voidtech.gerald.commands.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

public class AccentCommand extends AbstractCommand {

	private Map<String, String[]> getAccents() {
		Map<String, String[]> accents = new HashMap<String, String[]>();
		
		accents.put("a", "æãåāàáâä".split(""));
		accents.put("c", "ç".split(""));
		accents.put("e", "ēêëèé".split(""));
		accents.put("i", "ìïīîí".split(""));
		accents.put("n", "ñ".split(""));
		accents.put("o", "õōøœòöôó".split(""));
		accents.put("s", "ß".split(""));
		accents.put("u", "ūüùûú".split(""));
		
		return accents;
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		Map<String, String[]> accents = getAccents();
		List<String> characters = Arrays.asList(String.join(" ", args).toLowerCase().split(""));
		List<String> newCharacters = new ArrayList<String>();
		
		characters.forEach(character -> {
			if (accents.containsKey(character)) {
				newCharacters.add(accents.get(character)[new Random().nextInt(accents.get(character).length)]);
			} else {
				newCharacters.add(character);
			}
		});
		String finalMessage = String.join("", newCharacters);
		message.getChannel().sendMessage(finalMessage).queue();		
	}

	@Override
	public String getDescription() {
		return "Adds many accents to the letters in your words";
	}

	@Override
	public String getUsage() {
		return "accent a lovely message with many vowels";
	}

	@Override
	public String getName() {
		return "accent";
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
		String[] commandAliases = {"idekwhattocallthisone"};
		return commandAliases;
	}

}
