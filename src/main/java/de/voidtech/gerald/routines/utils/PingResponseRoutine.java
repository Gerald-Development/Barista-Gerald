package main.java.de.voidtech.gerald.routines.utils;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.ChatbotService;
import main.java.de.voidtech.gerald.service.GeraldConfigService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Routine
public class PingResponseRoutine extends AbstractRoutine {

	@Autowired
	private ServerService serverService;

    @Autowired
    private GeraldConfigService config;

    @Autowired
    private ChatbotService geraldAI;

    private void sendPingInfoMessage(Message message) {
    	Server guild = serverService.getServer(message.getGuild().getId());
		String prefix = guild.getPrefix() == null ? config.getDefaultPrefix() : guild.getPrefix();

		MessageEmbed pingResponseEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("You called? :telephone:", GlobalConstants.LINKTREE_URL)
				.setDescription("**This Guild's prefix is:** " + prefix + "\nTry " + prefix + "help to see some commands!")
				.build();
		message.getChannel().sendMessageEmbeds(pingResponseEmbed).queue();
    }

	@Override
	public void executeInternal(Message message) {
		if (message.getChannelType().equals(ChannelType.TEXT) && messageMentionsBot(message)) {
			List<String> messageBlocks = new ArrayList<>(Arrays.asList(message.getContentRaw().split(" ")));
			if (messageBlocks.size() == 1 && ParsingUtils.filterSnowflake(message.getContentRaw()).equals(message.getJDA().getSelfUser().getId()))
				sendPingInfoMessage(message);
			else {
				message.getChannel().sendTyping().queue();
				message.getChannel().sendMessage(geraldAI.getReply(message.getContentDisplay(), message.getId())).queue();
			}
		}
	}

	private boolean messageMentionsBot(Message message) {
		List<String> mentionedIds = message.getMentions().getMembers()
				.stream()
				.map(Member::getId)
				.collect(Collectors.toList());
		return mentionedIds.contains(message.getJDA().getSelfUser().getId());
	}

	@Override
	public String getDescription() {
		return "Allows Gerald to respond when he is mentioned";
	}

	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

	@Override
	public String getName() {
		return "r-ping";
	}

	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}

}