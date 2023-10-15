package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.persistence.entity.CountingChannel;
import main.java.de.voidtech.gerald.service.CountingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Listener
public class CountingMessageDeleteListener extends ListenerAdapter {

	@Autowired
	private CountingService countService;

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		CountingChannel channel = countService.getCountingChannel(event.getChannel().getId());
		if (channel != null) {
			if (event.getMessageId().equals(channel.getLastCountMessageId())) {
				event.getChannel().sendMessageEmbeds(createDeletedMessageEmbed()).queue();
			}
		}
	}

	private MessageEmbed createDeletedMessageEmbed() {
        return new EmbedBuilder()
                .setColor(Color.BLUE)
                .setTitle("Wait a minute! :warning:")
                .setDescription("**The last count was deleted**")
                .build();
	}
}