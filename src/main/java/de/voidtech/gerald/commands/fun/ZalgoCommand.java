package main.java.de.voidtech.gerald.commands.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;

@Command
public class ZalgoCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		Map<String, String> zalgoMap = getZalgoText();
		List<String> characters = Arrays.asList(String.join(" ", args).split(""));
		List<String> newCharacters = new ArrayList<String>();
		characters.forEach(character -> {
			if (zalgoMap.containsKey(character)) {
				newCharacters.add(zalgoMap.get(character));
			} else {
				newCharacters.add(character);
			}
		});
		String finalMessage = String.join("", newCharacters);
		message.getChannel().sendMessage(finalMessage).queue();
	}

	@Override
	public String getDescription() {
		return "Converts your messages into z̜̆͛̏̓̆̑̊a̸̱̦̪̫̯̹̫̙ͧ̍̆l͖ͬͥ̉ͣ͢g̢̳͆̌̆ͮo̶̭̭͎̍̎͋̍̓̊";
	}

	@Override
	public String getUsage() {
		return "zalgo a very normal message";
	}
	
	@Override
	public String getName() {
		return "zalgo";
	}
	private Map<String, String> getZalgoText() {
		Map<String, String> zalgoText = new HashMap<String, String>();
			zalgoText.put("a","a̸̱̦̪̫̯̹̫ͧ̍̆");
			zalgoText.put("b","b̵̻");
			zalgoText.put("c","c̳");
			zalgoText.put("d","d̰̰͎̥͈̦̗̊");
			zalgoText.put("e","e");
			zalgoText.put("f","f̥͈͍̯̦͍ͦͪ̂̚ͅ");
			zalgoText.put("g","g̢̳͆̌̆ͮ");
			zalgoText.put("h","hͨ̾͞");
			zalgoText.put("i","͔̙̝̬̖̗̦͛̈́ͬ̃̏̈́̆í̝̉̍͞");
			zalgoText.put("j","j̟͚͍ͅͅ");
			zalgoText.put("k","̠̱̱̪̮͌̇ͪ̏k̘̭̱̍ͣ̓ͪ̐̍͆͜");
			zalgoText.put("l","̙l͖ͬͥ̉ͣ͢");
			zalgoText.put("m","̩̟̗̠̱̟̯͑͛ͣͮ͑ṃ̢͍̾̓̉̚");
			zalgoText.put("n","͎n̪̬̗͍͈̱͔ͬ̓͆̀̂ͮ");
			zalgoText.put("o","o̶̭̭͎̍̎͋̍̓̊");
			zalgoText.put("p","̸͎͕̜̻̞͚͉͑̿p͌ͤ͊̅҉̳͈̬̤");
			zalgoText.put("q","͎͎͍̟̩̰͒̒͟q̯̆͋̉̿̕");
			zalgoText.put("r","̛̼̳̯͈ŗ̰̹͖̝̗͕ͮ̀͑ͤ͒̊");
			zalgoText.put("s","͉͓̙̲̮ͯ̿ͩ̈́̅͊̂s̟̼̗̊̈ͪ̓͐̎̾");
			zalgoText.put("t","̹̹ͤ̎ͨ͋ͅt̲͖͋ͦͭ̔");
			zalgoText.put("u","͊̏ͩ͛̒̂͛͠u̦");
			zalgoText.put("v","̸͍̘̖̞͔̳̽̓ͣͯ͆v̑̊ͯ̑͛");
			zalgoText.put("w","̫̞͍͈͖ͨͅͅw͙̱̪͇͙̬ͭ͒̽͊̍́ͅ");
			zalgoText.put("x","͔̼̟̜̫͚͈͌͊̄̕x͇̺̫͉͇̱̩ͧ͆̾͝");
			zalgoText.put("y","̲̳͙͇͉̝̇y̤̞̜̍̑̾̉̾̾ͮ͠");
			zalgoText.put("z","z̜̆͛̏̓̆̑̊");		
		
		return zalgoText;
	}

}
