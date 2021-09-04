package main.java.de.voidtech.gerald.commands.info;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.TwitchNotificationChannel;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.TwitchNotificationService;
import main.java.de.voidtech.gerald.util.MRESameUserPredicate;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
	
	private void getAwaitedReply(CommandContext context, String question, Consumer<String> result) {
        context.getChannel().sendMessage(question).queue();
        waiter.waitForEvent(MessageReceivedEvent.class,
                new MRESameUserPredicate(context.getAuthor()),
                event -> result.accept(event.getMessage().getContentRaw()), 30, TimeUnit.SECONDS,
                () -> context.getChannel().sendMessage("Request timed out.").queue());
    }
	
	private boolean validTwitchUrl(String streamerUrl) {
		return streamerUrl.matches(TWITCH_URL_MATCHER);
	}
	
	private boolean validChannelId(String channelId, CommandContext context) {
		return ParsingUtils.isSnowflake(channelId) && context.getGuild().getTextChannelById(channelId) != null;
	}
	
	private void removeStreamer(CommandContext context, List<String> args) {
		if (args.size() == 1)
			context.reply("**You need to specify a streamer to unsubscribe from!**");
		else {
			String streamer = args.get(1).toLowerCase();
			if (!twitchService.subscriptionExists(streamer, serverService.getServer(context.getGuild().getId()).getId()))
				context.reply("**A subscription to that streamer does not exist!**");
			else {
				twitchService.removeChannelSubscription(streamer, serverService.getServer(context.getGuild().getId()).getId());
				context.reply("**Subscription has been removed**");
			}
		}
	}

	private void listStreamers(CommandContext context) {
		List<TwitchNotificationChannel> subscriptions = twitchService.getAllSubscriptionsForServer(serverService.getServer(context.getGuild().getId()).getId());
		String messageBody = "";
		if (subscriptions.size() == 0)
			messageBody = "None to show!";
		else {
			for (TwitchNotificationChannel subscription : subscriptions) {
				messageBody += "**Streamer** - " + formatTwitchUrlMarkdown(subscription.getStreamerName()) + "\n**Channel** - <#" + subscription.getChannelId() + ">\n**Message** - " + subscription.getNotificationMessage() + "\n\n";
			}	
		}
		context.reply(buildTwitchSubscriptionEmbed(messageBody));
	}

	private String formatTwitchUrlMarkdown(String name) {
		return "[" + name + "](" + TWITCH_BASE_URL + name + ")";
	}

	private MessageEmbed buildTwitchSubscriptionEmbed(String messageBody) {
		return new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Twitch Subscriptions for this server")
				.setDescription(messageBody)
				.build();
	}

	private void addStreamer(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			streamerSetupGetStreamer(context);
		}
	}
	
	private void streamerSetupGetStreamer(CommandContext context) {
		getAwaitedReply(context, "**Please enter the twitch URL of the streamer you wish to subscribe to:**", streamerUrl -> {
			if (validTwitchUrl(streamerUrl)) {
				String streamerName = Arrays.asList(streamerUrl.split("/")).get(3).toLowerCase();
				if (twitchService.subscriptionExists(streamerName, serverService.getServer(context.getGuild().getId()).getId()))
					context.getChannel().sendMessage("**A subscription to that streamer already exists!**").queue();
				else
					streamerSetupGetChannel(context, streamerName);
			} else
				context.reply("**You did not enter a valid twitch.tv url! Please check your URL and start setup again**");
		});
	}

	private void streamerSetupGetChannel(CommandContext context, String streamerName) {
		getAwaitedReply(context, "**Please enter the channel you wish to get notifications sent to (use a channel mention or ID):**", rawChannelInput -> {
			String channelId = ParsingUtils.filterSnowflake(rawChannelInput);
			if (validChannelId(channelId, context)) {
				streamerSetupGetMessage(context, streamerName, channelId);
			} else {
				context.getChannel().sendMessage("**You did not enter a valid channel! Please check the mention was correct and the channel is in this server!**").queue();
			}
		});
	}

	private void streamerSetupGetMessage(CommandContext context, String streamerName, String channelId) {
		getAwaitedReply(context, "**Please enter your custom notification message:**", notificationMessage -> streamerSetupFinishSetup(context, streamerName, channelId, notificationMessage));
	}

	private void streamerSetupFinishSetup(CommandContext context, String streamerName, String channelId, String notificationMessage) {
		context.getChannel().sendMessage("**Setup completed!**\n\nStreamer Url: " + TWITCH_BASE_URL + streamerName + "\nChannel: <#" + channelId + ">\nNotification Message: " + notificationMessage).queue();
		twitchService.addSubscription(streamerName, channelId, notificationMessage, serverService.getServer(context.getGuild().getId()).getId());
	}

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		switch (args.get(0)) {		
		case "add":
			addStreamer(context);
			break;
		case "remove":
			removeStreamer(context, args);
			break;
		case "list":
			listStreamers(context);
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
		return new String[]{"tn"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}