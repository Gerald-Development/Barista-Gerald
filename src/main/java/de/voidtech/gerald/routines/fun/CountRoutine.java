package main.java.de.voidtech.gerald.routines.fun;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Objects;

import main.java.de.voidtech.gerald.entities.CountingChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.CountingChannel;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.CountingService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class CountRoutine extends AbstractRoutine {
	private final static String CORRECT = "U+2705";
	private final static String LETTER_N = "U+1F1F3";
	private final static String LETTER_I = "U+1F1EE";
	private final static String LETTER_C = "U+1F1E8";
	private final static String LETTER_E = "U+1F1EA";
	
	@Autowired
	private CountingService countService;

	@Autowired
	private CountingChannelRepository repository;

	private boolean shouldSendNice(int countGiven, String channelID, CountingChannel channel) {
		return ((countGiven == 69 || countGiven == -69) && !channel.hasReached69());
	}
	
	private void update69ReachedStatus(String channelID) {
		CountingChannel config = repository.getCountingChannelByChannelId(channelID);
		config.setReached69(true);
		config.setNumberOfTimes69HasBeenReached(config.get69ReachedCount() + 1);
		repository.save(config);
	}
	
	private void sendNice(Message message) {
		message.addReaction(LETTER_N).queue();
		message.addReaction(LETTER_I).queue();
		message.addReaction(LETTER_C).queue();
		message.addReaction(LETTER_E).queue();
		
		update69ReachedStatus(message.getChannel().getId());
	}
	
	private void playGame(Message message) {
		if (countService.isDifferentUser(Objects.requireNonNull(message.getMember()).getId(), message.getChannel().getId())) {
			CountingChannel channel = countService.getCountingChannel(message.getChannel().getId());
			int currentCount = channel.getChannelCount();
			int countGiven = Integer.parseInt(message.getContentRaw());
			
			if (countGiven == currentCount + 1) {
				countService.setCount(channel, currentCount, message.getMember().getId(), message.getId(), "increment");
				message.addReaction(CORRECT).queue();
				if (shouldSendNice(countGiven, message.getChannel().getId(), channel)) sendNice(message);
			
			} else if (countGiven == currentCount - 1) {
				countService.setCount(channel, currentCount, message.getMember().getId(), message.getId(), "decrement");
				message.addReaction(CORRECT).queue();
				if (shouldSendNice(countGiven, message.getChannel().getId(), channel)) sendNice(message);
			
			} else {
				channel.removeLife();
				if (channel.getLives() > 0) {
					countService.sendWarning(message, Color.MAGENTA, "You counted incorrectly!\nYou have " +
							channel.getLives() + " more chance" + (channel.getLives() != 1 ? "s" : "") + " before the count resets!");
					countService.saveCountConfig(channel);
				} else {
					countService.resetCount(message.getChannel().getId());
					countService.sendFailureMessage(message);	
				}
			}
		} else countService.sendWarning(message, Color.ORANGE, "You cannot count twice in a row!\nThe counter has not been reset.");
	}
	
	private void sendStatsMessage(CountingChannel channel, Message message) {
		message.getChannel().sendMessageEmbeds(countService.getCountStatsEmbedForChannel(channel, message.getJDA())).queue();
	}
	
	@Override
	public void executeInternal(Message message) {	
		CountingChannel channel = countService.getCountingChannel(message.getChannel().getId());
		EnumSet<Permission> perms = message.getGuild().getSelfMember().getPermissions((GuildChannel) message.getChannel());
		if (channel != null) {
			if (ParsingUtils.isInteger(message.getContentRaw()))
				playGame(message);
			else if (message.getContentRaw().equalsIgnoreCase("stats") |
					message.getContentRaw().equalsIgnoreCase("statistics"))
				sendStatsMessage(channel, message);
			else if (!channel.talkingIsAllowed()) {
				if (perms.contains(Permission.MESSAGE_MANAGE)) message.delete().queue();
			}
		}		
	}

	@Override
	public String getDescription() {
		return "Allows channels with counting enabled to work";
	}
	
	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	@Override
	public String getName() {
		return "r-count";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.FUN;
	}

}