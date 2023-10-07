package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.persistence.entity.CountingChannel;
import main.java.de.voidtech.gerald.persistence.repository.CountingChannelRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class CountingService {

    private final static Emoji INCORRECT = Emoji.fromUnicode("U+274C");

    @Autowired
    public CountingChannelRepository repository;

    public CountingChannel getCountingChannel(String channelID) {
        return repository.getCountingChannelByChannelId(channelID);
    }

    public void saveCountConfig(CountingChannel countChannel) {
        repository.save(countChannel);
    }

    public void stopCount(MessageChannel channel) {
        String channelID = channel.getId();
        repository.deleteCountingChannelByChannelId(channelID);
        channel.sendMessage("**This count has been ended. If you wish to start again, you will have to start from 0!**").queue();
    }

    public boolean isDifferentUser(String userID, String channelID) {
        return !getCountingChannel(channelID).getLastUser().equals(userID);
    }

    public void setCount(CountingChannel channel, int currentCount, String lastUserID, String lastMessageId, String mode) {
        int newCount = 0;
        if (mode.equals("increment")) newCount = currentCount + 1;
        else if (mode.equals("decrement")) newCount = currentCount - 1;

        channel.setChannelCount(newCount);
        channel.setLastUser(lastUserID);
        channel.setLastCountMessageId(lastMessageId);
        repository.save(channel);
    }

    public void resetCount(String channelID) {
        CountingChannel dbChannel = getCountingChannel(channelID);
        dbChannel.setChannelCount(0);
        dbChannel.setLastUser("");
        dbChannel.setReached69(false);
        dbChannel.resetLives();
        repository.save(dbChannel);
    }

    public void sendFailureMessage(Message message) {
        MessageEmbed failureEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("You failed! :no_entry:")
                .setDescription("**You failed! The counter has been reset to 0!**")
                .build();
        message.addReaction(INCORRECT).queue();
        message.getChannel().sendMessageEmbeds(failureEmbed).queue();
    }

    public void sendWarning(Message message, Color colour, String warning) {
        MessageEmbed warningEmbed = new EmbedBuilder()
                .setColor(colour)
                .setTitle("Wait a minute! :warning:")
                .setDescription("**" + warning + "**")
                .build();
        message.addReaction(INCORRECT).queue();
        message.getChannel().sendMessageEmbeds(warningEmbed).queue();
    }

    private String formatAsMarkdown(String input) {
        return "```\n" + input + "\n```";
    }

    public MessageEmbed getCountStatsEmbedForChannel(CountingChannel dbChannel, JDA jda) {
        String current = formatAsMarkdown(String.valueOf(dbChannel.getChannelCount()));
        String lastUser = formatAsMarkdown(dbChannel.getLastUser().equals("") ? "Nobody" : jda.getUserById(dbChannel.getLastUser()).getEffectiveName());
        String next = formatAsMarkdown(dbChannel.getChannelCount() - 1 + " or " + (dbChannel.getChannelCount() + 1));
        String reached69 = formatAsMarkdown(String.valueOf(dbChannel.hasReached69()));
        String numberOf69 = formatAsMarkdown(String.valueOf(dbChannel.get69ReachedCount()));
        String livesRemaining = formatAsMarkdown(String.valueOf(dbChannel.getLives()));

        return new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Counting Statistics")
                .addField("Current Count", current, true)
                .addField("Next Count", next, true)
                .addField("Last User", lastUser, false)
                .addField("Has reached 69?", reached69, true)
                .addField("No. of times 69 has been reached", numberOf69, true)
                .addField("Lives Remaining", livesRemaining, true)
                .build();
    }
}