package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NitroliteCommand extends AbstractCommand {
    private static final Logger LOGGER = Logger.getLogger(NitroliteCommand.class.getName());

    @Override
    public void executeInternal(Message message, List<String> args) {
        EnumSet<Permission> perms = message.getGuild().getSelfMember().getPermissions((GuildChannel) message.getChannel());
        Object[] emotes = message.getJDA().getEmoteCache().stream().filter(e -> e.getName().equals(args.get(0))).toArray();

        if (emotes.length > 0) {
            String content = StringUtils.join(args.subList(1, args.size()), " ") + " " +
                    (((Emote)emotes[0]).isAnimated() ? "<a:" : "<:") +
                    ((Emote)emotes[0]).getName() + ":" + ((Emote)emotes[0]).getId() + ">";

            if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
                if (!sendWebhookMessage(message, content)) {
                    ((TextChannel) message.getChannel()).createWebhook("BGnitrolite").complete();

                    if (!sendWebhookMessage(message, content)) {
                        super.sendErrorOccurred();
                        LOGGER.log(Level.INFO, "Error during CommandExecution: Webhook creation has failed");
                    }
                }
            } else {
                message.getChannel().sendMessage(content).queue();
                String pmString = "Barista-Gerald does not have permissions to create webhooks in this " +
                        "channel, contact the server owner to resolve this issue!";
                message.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(pmString)).queue();
            }
        }
    }

    private boolean sendWebhookMessage(Message message, String content) {
        List<Webhook> webhooks = ((TextChannel)message.getChannel()).retrieveWebhooks().complete();

        for (Webhook webhook : webhooks) {
            if (webhook.getName().equals("BGnitrolite")) {
                JSONObject webhookPayload = new JSONObject();
                webhookPayload.put("content", content);
                webhookPayload.put("username", message.getAuthor().getName());
                webhookPayload.put("avatar_url", message.getAuthor().getAvatarUrl());
                webhookPayload.put("tts", false);

                try {
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, webhookPayload.toString());
                    Request request = new Request.Builder()
                            .url(webhook.getUrl())
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    Response response = client.newCall(request).execute();

                    if (response.code() != 204) {
                        response.close();
                        throw new Exception("Webhook " + webhook.getName() + " from " + webhook.getChannel().getName()
                                + " did not respond with 204");
                    }
                    response.close();
                } catch (Exception ex) {
                    super.sendErrorOccurred();
                    LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + ex.getMessage());
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Enables you to use emotes from servers Barista-Gerald is on everywhere";
    }

    @Override
    public String getUsage() {
        return "emote_name [text](optional)";
    }
}
