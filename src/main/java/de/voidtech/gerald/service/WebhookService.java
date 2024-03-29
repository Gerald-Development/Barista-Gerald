package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Service
public class WebhookService {

    @Autowired
    private MultithreadingService multithreadingService;

    @Autowired
    private AlarmSenderService alarmService;

    public Webhook getOrCreateWebhook(TextChannel targetChannel, String webhookName, String selfID) {

        List<Webhook> webhooks = targetChannel.retrieveWebhooks().complete();
        for (Webhook webhook : webhooks) {
            if (webhook.getName().equals(webhookName) && Objects.requireNonNull(webhook.getOwnerAsUser()).getId().equals(selfID)) {
                return webhook;
            }
        }
        return targetChannel.createWebhook(webhookName).complete();
    }

    private void executeWebhookPost(String content, Message referencedMessage, String avatarUrl, String username, Webhook webhook) {
        String messageToBeSent = ParsingUtils.removeVolatileMentions(content);

        JSONObject webhookPayload = new JSONObject();
        webhookPayload.put("content", messageToBeSent);
        webhookPayload.put("username", username);
        webhookPayload.put("avatar_url", avatarUrl);
        webhookPayload.put("tts", false);

        if (referencedMessage != null) {
            MessageEmbed replyTextEmbed = new EmbedBuilder()
                    .setTitle("Replying to this message", referencedMessage.getJumpUrl())
                    .setDescription(referencedMessage.getContentRaw())
                    .setColor(new Color(47, 49, 54))
                    .setFooter("Original message from " + referencedMessage.getAuthor().getName(), referencedMessage.getAuthor().getAvatarUrl())
                    .build();
            webhookPayload.put("embeds", new JSONArray().put(new JSONObject(replyTextEmbed.toData().toString())));
        }

        try {
            URL url = new URL(webhook.getUrl());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Barista-Gerald");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStream stream = connection.getOutputStream();
            stream.write(webhookPayload.toString().getBytes());
            stream.flush();
            stream.close();

            connection.getInputStream().close();
            connection.disconnect();

        } catch (Exception ex) {
            alarmService.sendSystemAlarm(ex);
        }
    }

    public void postMessage(String content, Message referencedMessage, String avatarUrl, String username, Webhook webhook) {
        Runnable webhookThreadRunnable = () -> executeWebhookPost(content, referencedMessage, avatarUrl, username, webhook);
        multithreadingService.getThreadByName("T-Webhook").execute(webhookThreadRunnable);
    }

    public void postMessageWithFallback(CommandContext context, String content, String avatarUrl, String username, String webhookName) {
        EnumSet<Permission> perms = context.getGuild().getSelfMember().getPermissions(context.getGuildChannel());

        if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
            Webhook webhook = getOrCreateWebhook((TextChannel) context.getChannel(),
                    webhookName, context.getJDA().getSelfUser().getId());
            postMessage(content, null, avatarUrl, username, webhook);
        } else context.getChannel().sendMessage(content).queue();
    }

}