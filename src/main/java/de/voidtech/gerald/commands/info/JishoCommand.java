package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.playwright.Page;

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
		String search = JISHO_BASE_URL + String.join("+", args);
		byte[] resultImage = getJishoScreenshot(search);
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

	private byte[] getJishoScreenshot(String search) {
		Page jishoPage = playwrightService.getBrowser().newPage();
		jishoPage.navigate(search);
		jishoPage.setViewportSize(1200, 1500);
		byte[] screenshotBytesBuffer = jishoPage.screenshot();
		jishoPage.close();	
		
		return screenshotBytesBuffer;
	}

	@Override
	public String getDescription() {
		return "Allows you to get some translations from jisho! Jisho translates English to Japanese";
	}

	@Override
	public String getUsage() {
		return "jisho [english to be translated]";
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
