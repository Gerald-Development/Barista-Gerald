package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.TwitchNotificationChannel;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.TwitchNotificationService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Command
public class TwitchCommand extends AbstractCommand{

	@Autowired
	private EventWaiter waiter;
	
	@Autowired
	private TwitchNotificationService twitchService;
	
	@Autowired
	private ServerService serverService;
	
	private static final String TWITCH_BASE_URL = "https://twitch.tv/";
	private static final String TWITCH_URL_MATCHER = "https:\\/\\/(www\\.)?twitch.tv\\/.*";
	
	private void getAwaitedReply(Message message, String question, Consumer<String> result) {
        message.getChannel().sendMessage(question).queue();
        waiter.waitForEvent(MessageReceivedEvent.class,
                event -> event.getAuthor().getId().equals(message.getAuthor().getId()),
                event -> {
                    result.accept(event.getMessage().getContentRaw());
                }, 30, TimeUnit.SECONDS, 
                () -> message.getChannel().sendMessage(String.format("Request timed out.")).queue());
    }
	
	private boolean validTwitchUrl(String streamerUrl) {
		return streamerUrl.matches(TWITCH_URL_MATCHER);
	}
	
	private boolean validChannelId(String channelId, Message message) {
		return ParsingUtils.isSnowflake(channelId) && message.getGuild().getTextChannelById(channelId) != null;
	}
	
	private void removeStreamer(Message message, List<String> args) {
		if (args.size() == 1)
			message.getChannel().sendMessage("**You need to specify a streamer to unsubscribe from!**").queue();
		else {
			if (!twitchService.subscriptionExists(args.get(1), serverService.getServer(message.getGuild().getId()).getId()))
				message.getChannel().sendMessage("**A subscription to that streamer does not exist!**").queue();
			else {
				twitchService.removeChannelSubscription(args.get(1), serverService.getServer(message.getGuild().getId()).getId());
				message.getChannel().sendMessage("**Subscription has been removed**").queue();
			}
		}
	}

	private void listStreamers(Message message) {
		List<TwitchNotificationChannel> subscriptions = twitchService.getAllSubscriptionsForServer(serverService.getServer(message.getGuild().getId()).getId());
		String messageBody = "";
		if (subscriptions.size() == 0)
			messageBody = "None to show!";
		else {
			for (TwitchNotificationChannel subscription : subscriptions) {
				messageBody += "**Streamer** - " + formatTwitchUrlMarkdown(subscription.getStreamerName()) + "\n**Channel** - <#" + subscription.getChannelId() + ">\n**Message** - " + subscription.getNotificationMessage() + "\n\n";
			}	
		}
		message.getChannel().sendMessageEmbeds(buildTwitchSubscriptionEmbed(messageBody)).queue();
	}

	private String formatTwitchUrlMarkdown(String name) {
		return "[" + name + "](" + TWITCH_BASE_URL + name + ")";
	}

	private MessageEmbed buildTwitchSubscriptionEmbed(String messageBody) {
		MessageEmbed subscriptionEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Twitch Subscriptions for this server")
				.setDescription(messageBody)
				.build();
		return subscriptionEmbed;
	}

	private void addStreamer(Message message) {
		if (message.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			streamerSetupGetStreamer(message);
		}
	}
	
	private void streamerSetupGetStreamer(Message message) {
		getAwaitedReply(message, "**Please enter the twitch URL of the streamer you wish to subscribe to:**", streamerUrl -> {
			if (validTwitchUrl(streamerUrl)) {
				String streamerName = Arrays.asList(streamerUrl.split("/")).get(3);
				if (twitchService.subscriptionExists(streamerName, serverService.getServer(message.getGuild().getId()).getId()))
					message.getChannel().sendMessage("**A subscription to that streamer already exists!**").queue();
				else
					streamerSetupGetChannel(message, streamerName);
			} else
				message.getChannel().sendMessage("**You did not enter a valid twitch.tv url! Please check your URL and start setup again**").queue();	
		});
	}

	private void streamerSetupGetChannel(Message message, String streamerName) {
		getAwaitedReply(message, "**Please enter the channel you wish to get notifications sent to (use a channel mention or ID):**", rawChannelInput -> {
			String channelId = ParsingUtils.filterSnowflake(rawChannelInput);
			if (validChannelId(channelId, message)) {
				streamerSetupGetMessage(message, streamerName, channelId);
			} else {
				message.getChannel().sendMessage("**You did not enter a valid channel! Please check the mention was correct and the channel is in this server!**").queue();
			}
		});
	}

	private void streamerSetupGetMessage(Message message, String streamerName, String channelId) {
		getAwaitedReply(message, "**Please enter your custom notification message:**", notificationMessage -> {
			streamerSetupFinishSetup(message, streamerName, channelId, notificationMessage);	
		});	
	}

	private void streamerSetupFinishSetup(Message message, String streamerName, String channelId, String notificationMessage) {
		message.getChannel().sendMessage("**Setup completed!**\n\nStreamer Url: " + TWITCH_BASE_URL + streamerName + "\nChannel: <#" + channelId + ">\nNotification Message: " + notificationMessage).queue();
		twitchService.addSubscription(streamerName, channelId, notificationMessage, serverService.getServer(message.getGuild().getId()).getId());
	}

	@Override
	public void executeInternal(Message message, List<String> args) {
		switch (args.get(0)) {		
		case "add":
			addStreamer(message);
			break;
		case "remove":
			removeStreamer(message, args);
			break;
		case "list":
			listStreamers(message);
		}
	}

	@Override
	public String getDescription() {
		return "This system allows you to see when twitch streamers go live. Use the add command to add a streamer's notifications. You may choose a notification channel and a unique message for each streamer!";
	}

	@Override
	public String getUsage() {
		return "twitch add [then follow the instructions on screen]\n"
				+ "twitch remove [streamer name]\n"
				+ "twitch list";
	}

	@Override
	public String getName() {
		return "twitch";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.INFO;
	}

	@Override
	public boolean isDMCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}

	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"tn"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}