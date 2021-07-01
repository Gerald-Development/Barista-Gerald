package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.microsoft.playwright.Page;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.service.PlaywrightService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

@Command
public class GoogleCommand extends AbstractCommand {
	
	private static final String BROWSER_LOGO_IMAGE = "https://e7.pngegg.com/pngimages/293/824/png-clipart-ecosia-computer-icons-web-browser-android-illegal-logging-globe-logo-thumbnail.png";
	private static final String RED_CROSS_UNICODE = "U+274c";
	
	private static final String BROWSER_BASE_URL = "https://www.ecosia.org/";
	private static final String BROWSER_SEARCH_URL = "search?q=";
	private static final String BROWSER_NEWS_URL = "news?q=";
	private static final String BROWSER_IMAGES_URL = "images?q=";
	private static final String BROWSER_VIDEOS_URL = "videos?q=";
	private static final String SAFE_SEARCH_SUFFIX = "&sfs=true";

	@Autowired
	private EventWaiter waiter;
	
	@Autowired
	private PlaywrightService playwrightService;

	private String removeFirstListItem(List<String> originalList) {
		if (originalList.size() == 1) {
			return "";
		} else {
			List<String> modifiedList = new ArrayList<String>();
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
		} else 
			urlBuffer += BROWSER_SEARCH_URL;
		if (queryString == "")
			return null;
		else {
			urlBuffer += queryString;
			if (!nsfwAllowed)
				urlBuffer += SAFE_SEARCH_SUFFIX; 
			return urlBuffer;	
		}
	}

	private boolean getNsfwMode(MessageChannel messageChannel) 
	{
		return messageChannel.getType().equals(ChannelType.PRIVATE) ? true : ((TextChannel)messageChannel).isNSFW();
	}
	
	private void sendDeleteButtonWithListener(Message sentMessage, Message originMessage) {
		sentMessage.addReaction(RED_CROSS_UNICODE).queue();
		waiter.waitForEvent(MessageReactionAddEvent.class,
				event -> deleteEventAuthorised(((MessageReactionAddEvent) event), originMessage),
				event -> {
				boolean deleteButtonPressed = event.getReactionEmote().toString().equals("RE:" + RED_CROSS_UNICODE);
				if (deleteButtonPressed) {
					sentMessage.delete().queue();
				}
			}, 60, TimeUnit.SECONDS, () -> {});
	}
	
	private void sendFinalMessage(Message message, String url, byte[] screenshotBytesBuffer) {
		message.getChannel().sendMessage(constructResultEmbed(url, getNsfwMode(message.getChannel())))
		.addFile(screenshotBytesBuffer, "screenshot.png")
		.queue(sentMessage -> {
			if (!message.getChannelType().equals(ChannelType.PRIVATE)) {
				sendDeleteButtonWithListener(sentMessage, message);
			}
		});
	}

	private boolean deleteEventAuthorised(MessageReactionAddEvent event, Message message) {
		return !event.getMember().getId().equals(event.getJDA().getSelfUser().getId()) && 
				   (
						   event.getUser().getId().equals(message.getAuthor().getId()) ||
						   event.getMember().hasPermission(Permission.MESSAGE_MANAGE)
				   );
	}

	private MessageEmbed constructResultEmbed(String url, boolean safeSearchMode) {
		MessageEmbed googleEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("**Your Search Result:**", url)
				.setImage("attachment://screenshot.png")
				.setFooter("Powered By Ecosia | Safe mode " + (safeSearchMode ? "disabled" : "enabled"), BROWSER_LOGO_IMAGE)
				.build();
		return googleEmbed;
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		String url = constructSearchURL(args, getNsfwMode(message.getChannel()));
		
		if (url == null)
			message.getChannel().sendMessage("**You did not provide something to search for!**").queue();
		else {
			message.getChannel().sendTyping().queue();
			Page searchPage = playwrightService.getBrowser().newPage();
			searchPage.navigate(url);
			searchPage.setViewportSize(1000, 1000);
			
			byte[] screenshotBytesBuffer = searchPage.screenshot();
			searchPage.close();	
			
			sendFinalMessage(message, url, screenshotBytesBuffer);
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
		String[] aliases = {"search", "ecosia"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}