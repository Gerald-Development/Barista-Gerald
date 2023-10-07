package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class AlarmService {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private GeraldConfigService configService;

    public void sendCommandAlarm(String command, String reference, CommandContext context, Exception e) {
        TextChannel channel = context.getJDA().getTextChannelById(configService.getLoggingChannel());
        MessageEmbed alarmEmbed = new EmbedBuilder()
                .setTitle(":warning: Command " + command + " has failed :warning:")
                .addField(e.getMessage(), "```\n" + ExceptionUtils.getStackTrace(e) + "\n```", false)
                .setFooter("Reference: " + reference)
                .setColor(Color.RED)
                .build();
        channel.sendMessageEmbeds(alarmEmbed).queue();
        e.printStackTrace();
    }
}
