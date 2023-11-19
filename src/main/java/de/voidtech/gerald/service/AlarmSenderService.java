package main.java.de.voidtech.gerald.service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class AlarmSenderService {

    @Autowired
    private GeraldConfigService configService;

    private TextChannel loggingChannel;

    public void ready(JDA jda) {
        this.loggingChannel = jda.getTextChannelById(configService.getLoggingChannel());
    }

    public void sendCommandAlarm(String command, String reference, Exception e) {
        e.printStackTrace();
        MessageEmbed alarmEmbed = new EmbedBuilder()
                .setTitle(":warning: Command " + command + " has failed :warning:")
                .addField(shrink(e.getMessage(), 250), "```\n" + exceptionToString(e) + "\n```", false)
                .setFooter("Reference: " + reference)
                .setColor(Color.RED)
                .build();
        loggingChannel.sendMessageEmbeds(alarmEmbed).queue();
    }

    private String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionString = sw.toString();
        return shrink(exceptionString, 1000);
    }

    private String shrink(String input, int length) {
        return input.length() > length ? input.substring(0, length) + "..." : input;
    }

    public void sendSystemAlarm(Exception e) {
        MessageEmbed alarmEmbed = new EmbedBuilder()
                .setTitle(":warning: a system error has occurred :warning:")
                .addField(e.getMessage(), "```\n" + exceptionToString(e) + "\n```", false)
                .setColor(Color.BLACK)
                .build();
        loggingChannel.sendMessageEmbeds(alarmEmbed).queue();
        e.printStackTrace();
    }
}
