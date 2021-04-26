package main.java.de.voidtech.gerald.routines.utils;

import java.awt.Color;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Routine
public class PingResponseRoutine extends AbstractRoutine {

	@Autowired
	private ServerService serverService;
	
    @Autowired
    private GeraldConfig config;
	
	@Override
	public void executeInternal(Message message) {
		
		if (message.getChannelType().equals(ChannelType.TEXT)) {
			String strippedContent = message.getContentRaw()
					.replaceAll("<", "")
					.replaceAll("!", "")
					.replaceAll("@", "")
					.replaceAll(">", "");
			
			if (strippedContent.equals(message.getJDA().getSelfUser().getId())) {
				Server guild = serverService.getServer(message.getGuild().getId());
				String prefix = guild.getPrefix() == null ? config.getDefaultPrefix() : guild.getPrefix();
				new GlobalConstants();
				String linktree = GlobalConstants.LINKTREE_URL;
				
				MessageEmbed pingResponseEmbed = new EmbedBuilder()
						.setColor(Color.ORANGE)
						.setTitle("You called? :telephone:", linktree)
						.setDescription("**This Guild's prefix is:** " + prefix + "\nTry " + prefix + "help to see some commands!")
						.build();
				message.getChannel().sendMessage(pingResponseEmbed).queue();
			}	
		}
	}

	@Override
	public String getDescription() {
		return "Allows Gerald to respond when he is pinged";
	}

	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public String getName() {
		return "PingResponder";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}

}
