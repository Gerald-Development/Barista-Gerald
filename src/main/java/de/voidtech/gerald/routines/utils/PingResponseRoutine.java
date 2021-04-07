package main.java.de.voidtech.gerald.routines.utils;

import java.awt.Color;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Routine
public class PingResponseRoutine extends AbstractRoutine {

	@Autowired
	private ServerService serverService;
	
	@Override
	public void executeInternal(Message message) {
		if (message.getMentionedUsers().contains(message.getJDA().getSelfUser())) {
			Server guild = serverService.getServer(message.getGuild().getId());
			String prefix = guild.getPrefix();
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

	@Override
	public String getDescription() {
		return "Allows Gerald to respond when he is pinged";
	}

	@Override
	public boolean allowsBotResponses() {
		return false;
	}

}
