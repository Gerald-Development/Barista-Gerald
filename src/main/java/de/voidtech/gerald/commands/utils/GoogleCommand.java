package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

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
	
	private static final String BROWSER_LOGO_IMAGE = "https://e1.pngegg.com/pngimages/209/923/png-clipart-logo-google-g-suite-google-pay-google-doodle-texte-cercle-ligne-zone-thumbnail.png";
	private static final String RED_CROSS_UNICODE = "U+274c";
	
	private static final String BROWSER_BASE_URL = "https://www.google.com/search?q=";
	private static final String NEWS_MODE = "&tbm=nws";
	private static final String IMAGE_MODE = "&tbm=isch";
	private static final String VIDEO_MODE = "&tbm=vid";
	private static final String SAFE_SEARCH_SUFFIX = "&safe=active";

	@Autowired
	private EventWaiter waiter;
	
	@Autowired
	private PlaywrightService playwrightService;

	private String removeFirstListItem(List<String> originalList) {
		if (originalList.size() == 1) return "";
		else {
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
			String mode = "";
			queryString = String.join("+", removeFirstListItem(args));
			switch (flag) {
				case "i":
					mode = IMAGE_MODE;
					break;
				case "n":
					mode = NEWS_MODE;
					break;
				case "v":
					mode = VIDEO_MODE;
					break;
			}
			return (queryString == "") ? null : urlBuffer + queryString  + mode + (nsfwAllowed ? "" : SAFE_SEARCH_SUFFIX);
		} else return urlBuffer + queryString + (nsfwAllowed ? "" : SAFE_SEARCH_SUFFIX);
	}

	private boolean getNsfwMode(MessageChannel messageChannel) {
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
	
	private void sendFinalMessage(Message message, String url, byte[] screenshot) {
		message.getChannel().sendMessageEmbeds(constructResultEmbed(url, getNsfwMode(message.getChannel())))
		.addFile(screenshot, "screenshot.png")
		.queue(sentMessage -> {
			if (!message.getChannelType().equals(ChannelType.PRIVATE)) {
				sendDeleteButtonWithListener(sentMessage, message);
			}
		});
	}

	private boolean deleteEventAuthorised(MessageReactionAddEvent event, Message message) {
		return !event.getMember().getId().equals(event.getJDA().getSelfUser().getId()) && (
						   event.getUser().getId().equals(message.getAuthor().getId()) ||
						   event.getMember().hasPermission(Permission.MESSAGE_MANAGE));
	}

	private MessageEmbed constructResultEmbed(String url, boolean safeSearchMode) {
		MessageEmbed googleEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("**Your Search Result:**", url)
				.setImage("attachment://screenshot.png")
				.setFooter("Powered By Google | Safe mode " + (safeSearchMode ? "disabled" : "enabled"), BROWSER_LOGO_IMAGE)
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
			byte[] screenshot = playwrightService.screenshotPage(url, 1500, 1200);	
			
			sendFinalMessage(message, url, screenshot);
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
		String[] aliases = {"search"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}