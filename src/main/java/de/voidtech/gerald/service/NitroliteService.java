package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.persistence.entity.NitroliteEmote;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Service
public class NitroliteService {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private EmoteService emoteService;

    public void sendMessage(Message originMessage, String content) {

        EnumSet<Permission> perms = originMessage.getGuild().getSelfMember().getPermissions(originMessage.getGuildChannel());

        if (!originMessage.getAttachments().isEmpty()) {
            StringBuilder contentBuilder = new StringBuilder(content);
            for (Attachment attachment : originMessage.getAttachments()) {
                contentBuilder.append("\n").append(attachment.getUrl());
            }
            content = contentBuilder.toString();
        }

        if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
            sendWebhookMessage(originMessage, content);
            if (perms.contains(Permission.MESSAGE_MANAGE))
                originMessage.delete().complete();
        } else {
            if (perms.contains(Permission.MESSAGE_MANAGE)) {
                originMessage.delete().complete();
                sendRegularMessage(originMessage, content, true);
            } else {
                sendRegularMessage(originMessage, content, false);
            }
        }
    }

    private void sendRegularMessage(Message originMessage, String content, boolean canDeleteMessages) {
        String finalMessage = "";
        if (canDeleteMessages) {
            finalMessage += "**" + originMessage.getAuthor().getEffectiveName() + "**: ";
        }
        finalMessage += content;
        originMessage.getChannel().sendMessage(finalMessage).queue();
    }

    public String constructEmoteString(NitroliteEmote emote) {
        if (emote == null) return "[Emote Deleted]";
        else return String.format("<%s%s:%s>", emote.animated() ? "a:" : ":", emote.name(), emote.id());
    }

    private void sendWebhookMessage(Message message, String content) {
        Webhook webhook = webhookService.getOrCreateWebhook((TextChannel) message.getChannel(), "BGNitrolite", message.getJDA().getSelfUser().getId());
        webhookService.postMessage(content, message.getReferencedMessage(), message.getAuthor().getAvatarUrl(), Objects.requireNonNull(message.getMember()).getEffectiveName(), webhook);
    }

    public List<String> processNitroliteMessage(Message message) {
        List<String> messageTokens = Arrays.asList(message.getContentRaw().replaceAll("(?<! )\\[:", " [:").replaceAll(":](?! )", ":] ").split(" "));
        boolean foundOne = false;

        for (int i = 0; i < messageTokens.size(); i++) {
            String token = messageTokens.get(i);
            if (token.matches("\\[:[^:]*:]")) {
                String searchWord = token.substring(2, token.length() - 2);
                NitroliteEmote emoteOpt = emoteService.getEmoteByName(searchWord, message.getJDA());
                if (emoteOpt != null) {
                    foundOne = true;
                    messageTokens.set(i, constructEmoteString(emoteOpt));
                }
            }
        }
        return foundOne ? messageTokens : null;
    }
}