package main.java.de.voidtech.gerald.commands.effects;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;

@Command
public class ZalgoCommand extends AbstractCommand {

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		Map<String, String> zalgoMap = getZalgoText();
		List<String> characters = Arrays.asList(String.join(" ", args).toLowerCase().split(""));
		List<String> newCharacters = new ArrayList<>();
		characters.forEach(character -> newCharacters.add(zalgoMap.getOrDefault(character, character)));
		String finalMessage = String.join("", newCharacters);
		context.getChannel().sendMessage(finalMessage).queue();
	}

	@Override
	public String getDescription() {
		return "Converts your messages into Zalgo Text";
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
		Map<String, String> zalgoText = new HashMap<>();
			zalgoText.put("a", StringEscapeUtils.unescapeHtml4("a&#862;&#782;&#836;&#780;&#773;&#842;&#787;&#838;&#782;&#841;&#808;&#815;&#793;&#866;&#846;&#853;&#803;&#837;&#846;&#824;"));
			zalgoText.put("b", StringEscapeUtils.unescapeHtml4("b&#781;&#794;&#772;&#788;&#769;&#774;&#831;&#830;&#832;&#860;&#845;&#845;&#803;&#803;&#825;&#808;&#853;&#824;"));
			zalgoText.put("c", StringEscapeUtils.unescapeHtml4("c&#774;&#831;&#842;&#768;&#772;&#836;&#856;&#777;&#853;&#845;&#811;&#852;&#804;&#837;&#821;"));
			zalgoText.put("d", StringEscapeUtils.unescapeHtml4("d&#855;&#864;&#842;&#777;&#835;&#787;&#851;&#819;&#796;&#790;&#839;&#799;&#820;"));
			zalgoText.put("e", StringEscapeUtils.unescapeHtml4("e&#842;&#789;&#862;&#834;&#778;&#836;&#844;&#836;&#780;&#852;&#815;&#809;&#846;&#793;&#866;&#808;&#803;&#840;&#798;&#823;"));
			zalgoText.put("f", StringEscapeUtils.unescapeHtml4("f&#794;&#842;&#830;&#861;&#838;&#853;&#805;&#852;&#857;&#825;&#858;&#808;&#839;&#810;&#828;"));
			zalgoText.put("g", StringEscapeUtils.unescapeHtml4("g&#785;&#834;&#795;&#772;&#834;&#797;&#853;&#800;&#796;&#809;&#822;"));
			zalgoText.put("h", StringEscapeUtils.unescapeHtml4("h&#771;&#835;&#769;&#784;&#771;&#849;&#768;&#779;&#855;&#771;&#857;&#806;&#851;&#802;&#796;&#817;&#797;&#805;&#824;"));
			zalgoText.put("i", StringEscapeUtils.unescapeHtml4("i&#833;&#862;&#843;&#789;&#784;&#773;&#787;&#777;&#849;&#831;&#802;&#798;&#791;&#825;&#852;&#845;&#821;"));
			zalgoText.put("j", StringEscapeUtils.unescapeHtml4("j&#794;&#779;&#844;&#768;&#787;&#848;&#844;&#865;&#862;&#831;&#809;&#826;&#799;&#810;&#791;&#826;&#858;&#866;"));
			zalgoText.put("k", StringEscapeUtils.unescapeHtml4("k&#835;&#776;&#830;&#776;&#769;&#862;&#804;&#808;&#815;&#853;&#806;&#798;&#839;&#817;&#824;"));
			zalgoText.put("l", StringEscapeUtils.unescapeHtml4("l&#859;&#831;&#786;&#864;&#836;&#832;&#841;&#800;&#817;&#812;&#806;&#827;&#822;"));
			zalgoText.put("m", StringEscapeUtils.unescapeHtml4("m&#834;&#777;&#769;&#785;&#864;&#772;&#782;&#835;&#790;&#827;&#826;&#818;&#841;&#810;"));
			zalgoText.put("n", StringEscapeUtils.unescapeHtml4("n&#771;&#775;&#779;&#789;&#831;&#768;&#794;&#818;&#825;&#806;&#860;&#807;&#816;&#860;&#796;&#798;&#822;"));
			zalgoText.put("o", StringEscapeUtils.unescapeHtml4("o&#833;&#772;&#835;&#832;&#772;&#776;&#780;&#771;&#802;&#851;&#852;&#858;&#809;&#845;"));
			zalgoText.put("p", StringEscapeUtils.unescapeHtml4("p&#769;&#788;&#768;&#834;&#777;&#834;&#800;&#793;&#806;&#813;&#852;&#790;&#846;&#822;"));
			zalgoText.put("q", StringEscapeUtils.unescapeHtml4("q&#782;&#832;&#789;&#781;&#788;&#789;&#789;&#840;&#807;&#854;&#814;&#798;&#801;&#846;&#852;&#799;"));
			zalgoText.put("r", StringEscapeUtils.unescapeHtml4("r&#785;&#773;&#834;&#775;&#835;&#775;&#776;&#785;&#818;&#851;&#797;&#840;&#817;&#807;&#796;"));
			zalgoText.put("s", StringEscapeUtils.unescapeHtml4("s&#772;&#770;&#775;&#783;&#770;&#832;&#844;&#774;&#784;&#774;&#815;&#801;&#816;&#797;&#866;&#797;&#806;&#819;"));
			zalgoText.put("t", StringEscapeUtils.unescapeHtml4("t&#773;&#862;&#771;&#850;&#769;&#787;&#829;&#856;&#850;&#789;&#826;&#858;&#809;&#790;&#852;&#814;&#819;&#810;&#857;&#792;&#821;"));
			zalgoText.put("u", StringEscapeUtils.unescapeHtml4("u&#831;&#784;&#856;&#861;&#848;&#838;&#865;&#781;&#863;&#818;&#841;&#809;&#792;&#811;&#826;"));
			zalgoText.put("v", StringEscapeUtils.unescapeHtml4("v&#831;&#850;&#775;&#832;&#830;&#844;&#856;&#843;&#849;&#787;&#827;&#817;&#790;&#813;&#815;&#793;&#815;&#840;&#854;&#822;"));
			zalgoText.put("w", StringEscapeUtils.unescapeHtml4("w&#836;&#830;&#842;&#829;&#773;&#864;&#830;&#862;&#859;&#827;&#817;&#797;&#852;&#817;&#814;&#809;&#827;&#846;&#791;"));
			zalgoText.put("x", StringEscapeUtils.unescapeHtml4("x&#785;&#862;&#770;&#786;&#786;&#862;&#805;&#851;&#828;&#801;&#807;&#797;&#801;&#866;&#793;&#824;"));
			zalgoText.put("y", StringEscapeUtils.unescapeHtml4("y&#833;&#782;&#786;&#787;&#784;&#846;&#863;&#828;&#825;&#853;&#840;&#822;"));
			zalgoText.put("z", StringEscapeUtils.unescapeHtml4("z&#775;&#794;&#859;&#855;&#774;&#780;&#782;&#785;&#794;&#812;&#812;&#841;&#812;&#797;&#824;"));
		
		return zalgoText;
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
		return new String[]{"zalgoify", "fucktext"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
