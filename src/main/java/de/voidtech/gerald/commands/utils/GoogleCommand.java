package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.PlaywrightService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

@Command
public class GoogleCommand extends AbstractCommand {
	
	private static final String BROWSER_LOGO_IMAGE = "https://upload.wikimedia.org/wikipedia/en/archive/9/90/20211207123704%21The_DuckDuckGo_Duck.png";
	
	private static final String BROWSER_BASE_URL = "https://duckduckgo.com/";
	private static final String BROWSER_SEARCH_URL = "?ia=web&q=";
	private static final String BROWSER_NEWS_URL = "?ia=news&q=";
	private static final String BROWSER_IMAGES_URL = "?ia=images&q=";
	private static final String BROWSER_VIDEOS_URL = "?ia=videos&q=";
	
	private static final String SAFE_MODE_ENABLED = "&kp=1";
	private static final String SAFE_MODE_DISABLED = "&kp=-2";
	
	@Autowired
	private PlaywrightService playwrightService;

	private String removeFirstListItem(List<String> originalList) {
		if (originalList.size() == 1) {
			return "";
		} else {
			List<String> modifiedList = new ArrayList<>();
			for (int i = 1; i < originalList.size(); i++) {
				modifiedList.add(originalList.get(i));
			}
			return String.join("+", modifiedList);	
		}
	}
	
	private String constructSearchURL(List<String> args, boolean nsfwAllowed) {
		String urlBuffer = BROWSER_BASE_URL;
		String queryString = String.join("+", args);
		
		if (args.get(0).startsWith("-")) {
			String flag = args.get(0).substring(1);
			queryString = String.join("+", removeFirstListItem(args));
			switch (flag) {
				case "i":
					urlBuffer += BROWSER_IMAGES_URL;
					break;
				case "n":
					urlBuffer += BROWSER_NEWS_URL;
					break;
				case "v":
					urlBuffer += BROWSER_VIDEOS_URL;
					break;
				default:
					urlBuffer += BROWSER_SEARCH_URL;
					break;
			}
		} else urlBuffer += BROWSER_SEARCH_URL;
		if (queryString.equals(""))
			return null;
		else {
			urlBuffer += queryString; 
			urlBuffer += (nsfwAllowed ? SAFE_MODE_DISABLED : SAFE_MODE_ENABLED);
			return urlBuffer;	
		}
	}

	private boolean getNsfwMode(MessageChannel messageChannel) 
	{
		return messageChannel.getType().equals(ChannelType.PRIVATE) || ((TextChannel) messageChannel).isNSFW();
	}
	
	private void sendFinalMessage(CommandContext context, String url, byte[] screenshot) {
		context.replyWithFile(screenshot, "screenshot.png", constructResultEmbed(url, getNsfwMode(context.getChannel())));
	}

	private MessageEmbed constructResultEmbed(String url, boolean safeSearchMode) {
		return new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("**Your Search Result:**", url)
				.setImage("attachment://screenshot.png")
				.setFooter("Powered By DuckDuckGo | Safe mode " + (safeSearchMode ? "disabled" : "enabled"), BROWSER_LOGO_IMAGE)
				.build();
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String url = constructSearchURL(args, getNsfwMode(context.getChannel()));
		
		if (url == null)
			context.reply("**You did not provide something to search for!**");
		else {
			context.getChannel().sendTyping().queue();
			byte[] screenshot = playwrightService.screenshotPage(url, 1000, 1000);
			sendFinalMessage(context, url, screenshot);
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to search the mighty interwebs for something of interest";
	}

	@Override
	public String getUsage() {
		return "google [a thing you want to see]\n"
				+ "google -i [an image you want to see]\n"
				+ "google -n [a thing you want to know about]\n"
				+ "google -v [a thing you want to watch]\n"
				+ "use the flag -i for images, -v for videos and -n for news";
	}

	@Override
	public String getName() {
		return "google";
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

	@Override
	public String[] getCommandAliases() {
		return new String[]{"search", "ecosia"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
	@Override
	public boolean isSlashCompatible() {
		return true;
	}
}