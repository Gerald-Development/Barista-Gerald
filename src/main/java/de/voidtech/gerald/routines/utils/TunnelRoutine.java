package main.java.de.voidtech.gerald.routines.utils;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.persistence.entity.Tunnel;
import main.java.de.voidtech.gerald.persistence.repository.TunnelRepository;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.NitroliteService;
import main.java.de.voidtech.gerald.service.WebhookService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Routine
public class TunnelRoutine extends AbstractRoutine {

	@Autowired
	private TunnelRepository repository;

	@Autowired
	private WebhookService webhookService;

	@Autowired
	private NitroliteService nitroliteService;

	private boolean tunnelExists(String senderChannelID) {
		return repository.getTunnelBySingleChannelId(senderChannelID) != null;
	}

	private boolean targetChannelExists(Tunnel tunnel, Message message) {
		String sourceChannelId = message.getChannel().getId();
		String targetChannelId = tunnel.getSourceChannel().equals(sourceChannelId)
				? tunnel.getDestChannel()
				: tunnel.getSourceChannel();

		return message.getJDA().getTextChannelById(targetChannelId) != null;
	}

	private Tunnel getTunnel(String senderChannelID) {
		return repository.getTunnelBySingleChannelId(senderChannelID);
	}

	private void sendWebhookMessage(Webhook webhook, String content, Message message) {
		if (message.getAttachments().size() != 0) {
			StringBuilder contentBuilder = new StringBuilder(content);
			for (Attachment attachment: message.getAttachments()) {
				contentBuilder.append("\n").append(attachment.getUrl());
			}
			content = contentBuilder.toString();
		}

		webhookService.postMessage(content, message.getReferencedMessage(), message.getAuthor().getAvatarUrl(), message.getAuthor().getName(), webhook);
	}

	private void sendTunnelMessage(Tunnel tunnel, Message message) {
		String messageContent = ParsingUtils.removeVolatileMentions(message.getContentRaw());
		TextChannel channel = tunnel.getSourceChannel().equals(message.getChannel().getId())
				? message.getJDA().getTextChannelById(tunnel.getDestChannel())
				: message.getJDA().getTextChannelById(tunnel.getSourceChannel());

		List<String> processedNitroliteMessage = nitroliteService.processNitroliteMessage(message);
		if (processedNitroliteMessage != null) messageContent = String.join(" ", processedNitroliteMessage);

		EnumSet<Permission> perms = Objects.requireNonNull(channel).getGuild().getSelfMember().getPermissions(channel);
		if (perms.contains(Permission.MANAGE_WEBHOOKS)) {
			Webhook webhook = webhookService.getOrCreateWebhook(channel, "BGTunnel", message.getJDA().getSelfUser().getId());
			sendWebhookMessage(webhook, messageContent, message);
		} else channel.sendMessage("**" + message.getAuthor().getEffectiveName() + ":** " + messageContent).queue();
	}

	private void deleteTunnel(String channelId) {
		repository.deleteTunnel(channelId);
	}

	private void deleteTunnelAndSendError(Message message) {
		message.getChannel().sendMessage("**This tunnel no longer exists! It has now been deleted.**").queue();
		deleteTunnel(message.getChannel().getId());
	}

	@Override
	public void executeInternal(Message message) {
		if (message.getAuthor().getId().equals(message.getJDA().getSelfUser().getId())) return;
		if (message.getContentRaw().isEmpty()) return;
		if (tunnelExists(message.getChannel().getId())) {
			Tunnel tunnel = getTunnel(message.getChannel().getId());
			if (targetChannelExists(tunnel, message))
				sendTunnelMessage(getTunnel(message.getChannel().getId()), message);
			else
				deleteTunnelAndSendError(message);
		}
	}

	@Override
	public String getDescription() {
		return "Allows tunnels to be handled";
	}

	@Override
	public boolean allowsBotResponses() {
		return true;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	@Override
	public String getName() {
		return "r-tunnel";
	}

	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}
}