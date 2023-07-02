package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.persistence.entity.CountingChannel;
import main.java.de.voidtech.gerald.service.CountingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class CountingMessageDeleteListener implements EventListener {

	@Autowired
	private CountingService countService;
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof MessageDeleteEvent) {
			MessageDeleteEvent message = (MessageDeleteEvent) event;
			CountingChannel channel = countService.getCountingChannel(message.getChannel().getId());
			if (channel != null) {
				if (message.getMessageId().equals(channel.getLastCountMessageId())) {
					message.getChannel().sendMessageEmbeds(createDeletedMessageEmbed()).queue();
				}
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