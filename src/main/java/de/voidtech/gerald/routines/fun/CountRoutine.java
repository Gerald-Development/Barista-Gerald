package main.java.de.voidtech.gerald.routines.fun;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.persistence.entity.CountingChannel;
import main.java.de.voidtech.gerald.persistence.repository.CountingChannelRepository;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.CountingService;
import main.java.de.voidtech.gerald.util.ArithmeticUtils;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.EnumSet;

@Routine
public class CountRoutine extends AbstractRoutine {
	private final static Emoji CORRECT = Emoji.fromUnicode("U+2705");
	private final static Emoji LETTER_N = Emoji.fromUnicode("U+1F1F3");
	private final static Emoji LETTER_I = Emoji.fromUnicode("U+1F1EE");
	private final static Emoji LETTER_C = Emoji.fromUnicode("U+1F1E8");
	private final static Emoji LETTER_E = Emoji.fromUnicode("U+1F1EA");
	
	@Autowired
	private CountingService countService;

	@Autowired
	private CountingChannelRepository repository;

	private boolean shouldSendNice(int countGiven, CountingChannel channel) {
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
	
	private void playGame(Message message, int countGiven) {
		if (countService.isDifferentUser(message.getMember().getId(), message.getChannel().getId())) {
			CountingChannel channel = countService.getCountingChannel(message.getChannel().getId());
			int currentCount = channel.getChannelCount();
			
			if (countGiven == currentCount + 1) {
				countService.setCount(channel, currentCount, message.getMember().getId(), message.getId(), "increment");
				message.addReaction(CORRECT).queue();
				if (shouldSendNice(countGiven, channel)) sendNice(message);
			
			} else if (countGiven == currentCount - 1) {
				countService.setCount(channel, currentCount, message.getMember().getId(), message.getId(), "decrement");
				message.addReaction(CORRECT).queue();
				if (shouldSendNice(countGiven, channel)) sendNice(message);
			
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
		EnumSet<Permission> perms = message.getGuild().getSelfMember().getPermissions(message.getGuildChannel());
		if (channel != null) {

			if (ArithmeticUtils.isValidExpression(message.getContentRaw())) {
				try {
					double result = ArithmeticUtils.evalExpression(message.getContentRaw(), System.currentTimeMillis());
					int count = (int) Math.round(result);
					playGame(message, count);
				} catch (Exception e) {
					message.reply("**Your expression is invalid**").queue();
				}
			} else if (ParsingUtils.isInteger(message.getContentRaw())) {
				playGame(message, Integer.parseInt(message.getContentRaw()));
			} else if (message.getContentRaw().equalsIgnoreCase("stats") | message.getContentRaw().equalsIgnoreCase("statistics")) {
				sendStatsMessage(channel, message);
			} else if (!channel.talkingIsAllowed() && perms.contains(Permission.MESSAGE_MANAGE)) {
				message.delete().queue();
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