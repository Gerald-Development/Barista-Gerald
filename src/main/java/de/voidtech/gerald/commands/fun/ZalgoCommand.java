package main.java.de.voidtech.gerald.commands.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
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
		return "Converts your messages into zÃŒâ€ Ã�â€ºÃŒï¿½ÃŒâ€œÃŒâ€ ÃŒâ€˜ÃŒÅ ÃŒÅ“aÃ�Â§ÃŒï¿½ÃŒâ€ ÃŒÂ¸ÃŒÂ±ÃŒÂ¦ÃŒÂªÃŒÂ«ÃŒÂ¯ÃŒÂ¹ÃŒÂ«ÃŒâ„¢lÃ�Â¬Ã�Â¥ÃŒâ€°Ã�Â£Ã�Â¢Ã�â€“gÃ�â€ ÃŒÅ’ÃŒâ€ Ã�Â®ÃŒÂ¢ÃŒÂ³oÃŒï¿½ÃŒÅ½Ã�â€¹ÃŒï¿½ÃŒâ€œÃŒÅ ÃŒÂ¶ÃŒÂ­ÃŒÂ­Ã�Å½";
	}

	@Override
	public String getUsage() {
		return "zalgo a very normal message";
	}
	
	@Override
	public String getName() {
		return "zalgo";
	}
	@SuppressWarnings("deprecation")
	private Map<String, String> getZalgoText() {
		Map<String, String> zalgoText = new HashMap<String, String>();
			zalgoText.put("a", StringEscapeUtils.unescapeHtml4("a&#862;&#782;&#836;&#780;&#773;&#842;&#787;&#838;&#782;&#841;&#808;&#815;&#793;&#866;&#846;&#853;&#803;&#837;&#846;&#824;"));
			zalgoText.put("b","bÃŒÂµÃŒÂ»");
			zalgoText.put("c","cÃŒÂ³");
			zalgoText.put("d","dÃŒÅ ÃŒÂ°ÃŒÂ°Ã�Å½ÃŒÂ¥Ã�Ë†ÃŒÂ¦ÃŒâ€”");
			zalgoText.put("e","e");
			zalgoText.put("f","fÃ�Â¦ÃŒÅ¡Ã�ÂªÃŒâ€šÃŒÂ¥Ã�â€¦Ã�Ë†Ã�ï¿½ÃŒÂ¯ÃŒÂ¦Ã�ï¿½");
			zalgoText.put("g","gÃ�â€ ÃŒÅ’ÃŒâ€ Ã�Â®ÃŒÂ¢ÃŒÂ³");
			zalgoText.put("h","hÃ�Â¨ÃŒÂ¾Ã�Å¾");
			zalgoText.put("i","Ã�â€ºÃ�â€žÃ�Â¬ÃŒÆ’ÃŒï¿½Ã�â€žÃŒâ€ Ã�â€�ÃŒâ„¢ÃŒï¿½ÃŒÂ¬ÃŒâ€“ÃŒâ€”ÃŒÂ¦iÃŒï¿½ÃŒâ€°ÃŒï¿½Ã�Å¾ÃŒï¿½");
			zalgoText.put("j","jÃŒÅ¸Ã�â€¦Ã�Å¡Ã�â€¦Ã�ï¿½");
			zalgoText.put("k","Ã�Å’ÃŒâ€¡Ã�ÂªÃŒï¿½ÃŒÂ ÃŒÂ±ÃŒÂ±ÃŒÂªÃŒÂ®kÃŒï¿½Ã�Â£ÃŒâ€œÃ�ÂªÃŒï¿½ÃŒï¿½Ã�â€ Ã�Å“ÃŒËœÃŒÂ­ÃŒÂ±");
			zalgoText.put("l","ÃŒâ„¢lÃ�Â¬Ã�Â¥ÃŒâ€°Ã�Â£Ã�Â¢Ã�â€“");
			zalgoText.put("m","Ã�â€˜Ã�â€ºÃ�Â£Ã�Â®Ã�â€˜ÃŒÂ©ÃŒÅ¸ÃŒâ€”ÃŒÂ ÃŒÂ±ÃŒÅ¸ÃŒÂ¯mÃŒÂ¾ÃŒÅ¡Ã�Æ’ÃŒâ€°ÃŒÂ¢ÃŒÂ£Ã�ï¿½");
			zalgoText.put("n","Ã�Å½nÃ�Â¬Ã�Æ’Ã�â€ ÃŒâ‚¬ÃŒâ€šÃ�Â®ÃŒÂªÃŒÂ¬ÃŒâ€”Ã�ï¿½Ã�Ë†ÃŒÂ±Ã�â€�");
			zalgoText.put("o","oÃŒï¿½ÃŒÅ½Ã�â€¹ÃŒï¿½ÃŒâ€œÃŒÅ ÃŒÂ¶ÃŒÂ­ÃŒÂ­Ã�Å½");
			zalgoText.put("p","Ã�â€˜ÃŒÂ¿ÃŒÂ¸Ã�Å½Ã�â€¢ÃŒÅ“ÃŒÂ»ÃŒÅ¾Ã�Å¡Ã�â€°pÃ�Å’Ã�Â¤Ã�Å ÃŒâ€¦Ã’â€°ÃŒÂ³Ã�Ë†ÃŒÂ¬ÃŒÂ¤");
			zalgoText.put("q","Ã�â€™ÃŒâ€™Ã�Å¸Ã�Å½Ã�Å½Ã�ï¿½ÃŒÅ¸ÃŒÂ©ÃŒÂ°qÃŒâ€ Ã�â€¹ÃŒâ€°ÃŒÂ¿ÃŒâ€¢ÃŒÂ¯");
			zalgoText.put("r","ÃŒâ€ºÃŒÂ¼ÃŒÂ³ÃŒÂ¯Ã�Ë†rÃ�Â®ÃŒâ‚¬Ã�â€˜Ã�Â¤Ã�â€™ÃŒÅ ÃŒÂ§ÃŒÂ°ÃŒÂ¹Ã�â€“ÃŒï¿½ÃŒâ€”Ã�â€¢");
			zalgoText.put("s","Ã�Â¯ÃŒÂ¿Ã�Â©Ã�â€žÃŒâ€¦Ã�Å ÃŒâ€šÃ�â€°Ã�â€œÃŒâ„¢ÃŒÂ²ÃŒÂ®sÃŒÅ ÃŒË†Ã�ÂªÃŒâ€œÃ�ï¿½ÃŒÅ½ÃŒÂ¾ÃŒÅ¸ÃŒÂ¼ÃŒâ€”");
			zalgoText.put("t","Ã�Â¤ÃŒÅ½Ã�Â¨Ã�â€¹ÃŒÂ¹Ã�â€¦ÃŒÂ¹tÃ�â€¹Ã�Â¦Ã�Â­ÃŒâ€�ÃŒÂ²Ã�â€“");
			zalgoText.put("u","Ã�Å ÃŒï¿½Ã�Â©Ã�â€ºÃŒâ€™ÃŒâ€šÃ�â€ºÃ�Â uÃŒÂ¦");
			zalgoText.put("v","ÃŒÂ½ÃŒâ€œÃ�Â£Ã�Â¯Ã�â€ ÃŒÂ¸Ã�ï¿½ÃŒËœÃŒâ€“ÃŒÅ¾Ã�â€�ÃŒÂ³vÃŒâ€˜ÃŒÅ Ã�Â¯ÃŒâ€˜Ã�â€º");
			zalgoText.put("w","Ã�Â¨Ã�â€¦Ã�â€¦ÃŒÂ«ÃŒÅ¾Ã�ï¿½Ã�Ë†Ã�â€“wÃ�Â­Ã�â€™ÃŒÂ½Ã�Å ÃŒï¿½ÃŒï¿½Ã�â„¢ÃŒÂ±Ã�â€¦ÃŒÂªÃ�â€¡Ã�â„¢ÃŒÂ¬");
			zalgoText.put("x","Ã�Å’Ã�Å ÃŒâ€žÃŒâ€¢Ã�â€�ÃŒÂ¼ÃŒÅ¸ÃŒÅ“ÃŒÂ«Ã�Å¡Ã�Ë†xÃ�Â§Ã�â€ ÃŒÂ¾Ã�ï¿½Ã�â€¡ÃŒÂºÃŒÂ«Ã�â€°Ã�â€¡ÃŒÂ±ÃŒÂ©");
			zalgoText.put("y","ÃŒâ€¡ÃŒÂ²ÃŒÂ³Ã�â„¢Ã�â€¡Ã�â€°ÃŒï¿½yÃŒï¿½ÃŒâ€˜ÃŒÂ¾ÃŒâ€°ÃŒÂ¾ÃŒÂ¾Ã�Â®Ã�Â ÃŒÂ¤ÃŒÅ¾ÃŒÅ“");
			zalgoText.put("z","zÃŒâ€ Ã�â€ºÃŒï¿½ÃŒâ€œÃŒâ€ ÃŒâ€˜ÃŒÅ ÃŒÅ“");		
		
		return zalgoText;
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
