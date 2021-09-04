package main.java.de.voidtech.gerald.commands.effects;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.*;

@Command
public class AccentCommand extends AbstractCommand {

	private Map<String, String[]> getAccents() {
		Map<String, String[]> accents = new HashMap<>();
		
		accents.put("a", "æãåāàáâä".split(""));
		accents.put("c", "ç".split(""));
		accents.put("e", "ēêëèé".split(""));
		accents.put("i", "ìïīîí".split(""));
		accents.put("n", "ñ".split(""));
		accents.put("o", "õōøœòöôó".split(""));
		accents.put("u", "ūüùûú".split(""));
		
		return accents;
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		Map<String, String[]> accents = getAccents();
		List<String> characters = Arrays.asList(String.join(" ", args).toLowerCase().split(""));
		List<String> newCharacters = new ArrayList<>();
		
		characters.forEach(character -> {
			if (accents.containsKey(character)) {
				newCharacters.add(accents.get(character)[new Random().nextInt(accents.get(character).length)]);
			} else {
				newCharacters.add(character);
			}
		});
		String finalMessage = String.join("", newCharacters);
		context.reply(finalMessage);
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
		return new String[]{"a"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
