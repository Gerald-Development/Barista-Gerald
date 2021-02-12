package main.java.de.voidtech.gerald.commands.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		return "Converts your messages into zÌ†Í›Ì�Ì“Ì†Ì‘ÌŠÌœaÍ§Ì�Ì†Ì¸Ì±Ì¦ÌªÌ«Ì¯Ì¹Ì«Ì™lÍ¬Í¥Ì‰Í£Í¢Í–gÍ†ÌŒÌ†Í®Ì¢Ì³oÌ�ÌŽÍ‹Ì�Ì“ÌŠÌ¶Ì­Ì­ÍŽ";
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
			zalgoText.put("a","aÍ§Ì�Ì†Ì¸Ì±Ì¦ÌªÌ«Ì¯Ì¹Ì«");
			zalgoText.put("b","bÌµÌ»");
			zalgoText.put("c","cÌ³");
			zalgoText.put("d","dÌŠÌ°Ì°ÍŽÌ¥ÍˆÌ¦Ì—");
			zalgoText.put("e","e");
			zalgoText.put("f","fÍ¦ÌšÍªÌ‚Ì¥Í…ÍˆÍ�Ì¯Ì¦Í�");
			zalgoText.put("g","gÍ†ÌŒÌ†Í®Ì¢Ì³");
			zalgoText.put("h","hÍ¨Ì¾Íž");
			zalgoText.put("i","Í›Í„Í¬ÌƒÌ�Í„Ì†Í”Ì™Ì�Ì¬Ì–Ì—Ì¦iÌ�Ì‰Ì�ÍžÌ�");
			zalgoText.put("j","jÌŸÍ…ÍšÍ…Í�");
			zalgoText.put("k","ÍŒÌ‡ÍªÌ�Ì Ì±Ì±ÌªÌ®kÌ�Í£Ì“ÍªÌ�Ì�Í†ÍœÌ˜Ì­Ì±");
			zalgoText.put("l","Ì™lÍ¬Í¥Ì‰Í£Í¢Í–");
			zalgoText.put("m","Í‘Í›Í£Í®Í‘Ì©ÌŸÌ—Ì Ì±ÌŸÌ¯mÌ¾ÌšÍƒÌ‰Ì¢Ì£Í�");
			zalgoText.put("n","ÍŽnÍ¬ÍƒÍ†Ì€Ì‚Í®ÌªÌ¬Ì—Í�ÍˆÌ±Í”");
			zalgoText.put("o","oÌ�ÌŽÍ‹Ì�Ì“ÌŠÌ¶Ì­Ì­ÍŽ");
			zalgoText.put("p","Í‘Ì¿Ì¸ÍŽÍ•ÌœÌ»ÌžÍšÍ‰pÍŒÍ¤ÍŠÌ…Ò‰Ì³ÍˆÌ¬Ì¤");
			zalgoText.put("q","Í’Ì’ÍŸÍŽÍŽÍ�ÌŸÌ©Ì°qÌ†Í‹Ì‰Ì¿Ì•Ì¯");
			zalgoText.put("r","Ì›Ì¼Ì³Ì¯ÍˆrÍ®Ì€Í‘Í¤Í’ÌŠÌ§Ì°Ì¹Í–Ì�Ì—Í•");
			zalgoText.put("s","Í¯Ì¿Í©Í„Ì…ÍŠÌ‚Í‰Í“Ì™Ì²Ì®sÌŠÌˆÍªÌ“Í�ÌŽÌ¾ÌŸÌ¼Ì—");
			zalgoText.put("t","Í¤ÌŽÍ¨Í‹Ì¹Í…Ì¹tÍ‹Í¦Í­Ì”Ì²Í–");
			zalgoText.put("u","ÍŠÌ�Í©Í›Ì’Ì‚Í›Í uÌ¦");
			zalgoText.put("v","Ì½Ì“Í£Í¯Í†Ì¸Í�Ì˜Ì–ÌžÍ”Ì³vÌ‘ÌŠÍ¯Ì‘Í›");
			zalgoText.put("w","Í¨Í…Í…Ì«ÌžÍ�ÍˆÍ–wÍ­Í’Ì½ÍŠÌ�Ì�Í™Ì±Í…ÌªÍ‡Í™Ì¬");
			zalgoText.put("x","ÍŒÍŠÌ„Ì•Í”Ì¼ÌŸÌœÌ«ÍšÍˆxÍ§Í†Ì¾Í�Í‡ÌºÌ«Í‰Í‡Ì±Ì©");
			zalgoText.put("y","Ì‡Ì²Ì³Í™Í‡Í‰Ì�yÌ�Ì‘Ì¾Ì‰Ì¾Ì¾Í®Í Ì¤ÌžÌœ");
			zalgoText.put("z","zÌ†Í›Ì�Ì“Ì†Ì‘ÌŠÌœ");		
		
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
