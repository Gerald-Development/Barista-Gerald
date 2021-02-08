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
public class EmojifyCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		Map<String, String> emojiMap = getemojiText();
		List<String> characters = Arrays.asList(String.join(" ", args).split(""));
		List<String> newCharacters = new ArrayList<String>();
		characters.forEach(character -> {
			if (emojiMap.containsKey(character)) {
				newCharacters.add(emojiMap.get(character));
			} else {
				newCharacters.add(character);
			}
		});
		String finalMessage = String.join("", newCharacters);
		message.getChannel().sendMessage(finalMessage).queue();
	}

	@Override
	public String getDescription() {
		return "Converts your messages into emoji text";
	}

	@Override
	public String getUsage() {
		return "emojify a very normal message";
	}
	
	private Map<String, String> getemojiText() {
		Map<String, String> emojiText = new HashMap<String, String>();
			emojiText.put("a",":regional_indicator_a:");
			emojiText.put("b",":regional_indicator_b:");
			emojiText.put("c",":regional_indicator_c:");
			emojiText.put("d",":regional_indicator_d:");
			emojiText.put("e",":regional_indicator_e:");
			emojiText.put("f",":regional_indicator_f:");
			emojiText.put("g",":regional_indicator_g:");
			emojiText.put("h",":regional_indicator_h:");
			emojiText.put("i",":regional_indicator_i:");
			emojiText.put("j",":regional_indicator_j:");
			emojiText.put("k",":regional_indicator_k:");
			emojiText.put("l",":regional_indicator_l:");
			emojiText.put("m",":regional_indicator_m:");
			emojiText.put("n",":regional_indicator_n:");
			emojiText.put("o",":regional_indicator_o:");
			emojiText.put("p",":regional_indicator_p:");
			emojiText.put("q",":regional_indicator_q:");
			emojiText.put("r",":regional_indicator_r:");
			emojiText.put("s",":regional_indicator_s:");
			emojiText.put("t",":regional_indicator_t:");
			emojiText.put("u",":regional_indicator_u:");
			emojiText.put("v",":regional_indicator_v:");
			emojiText.put("w",":regional_indicator_w:");
			emojiText.put("x",":regional_indicator_x:");
			emojiText.put("y",":regional_indicator_y:");
			emojiText.put("z",":regional_indicator_z:");
		
		return emojiText;
	}

	@Override
	public String getName() {
		return "emojify";
	}
}
