package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.service.PlaywrightService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class JishoCommand extends AbstractCommand {

	@Autowired
	private PlaywrightService playwrightService;
	
	private static final String JISHO_BASE_URL = "https://jisho.org/search/";
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		message.getChannel().sendTyping().queue();
		String search = JISHO_BASE_URL + String.join("%20", args).replaceAll("#", "%23");
		byte[] resultImage = playwrightService.screenshotPage(search, 1500, 1500);
		message.getChannel().sendMessageEmbeds(constructResultEmbed(search))
		.addFile(resultImage, "screenshot.png").queue();
	}
	
	private MessageEmbed constructResultEmbed(String url) {
		MessageEmbed jishoEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("**Your Search Result:**", url)
				.setImage("attachment://screenshot.png")
				.setFooter("Powered by Jisho")
				.build();
		return jishoEmbed;
	}

	@Override
	public String getDescription() {
		return "Allows you to interact with Jisho! Jisho is a powerful Japanese-English dictionary. It lets you find words, kanji, example sentences and more quickly and easily. (https://jisho.org/)";
	}

	@Override
	public String getUsage() {
		return "jisho [something to search for]";
	}

	@Override
	public String getName() {
		return "jisho";
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
		String[] aliases = {"japanese"};
		return aliases;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
