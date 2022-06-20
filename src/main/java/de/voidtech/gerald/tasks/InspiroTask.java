package main.java.de.voidtech.gerald.tasks;

import java.awt.Color;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Task;
import main.java.de.voidtech.gerald.service.InspiroService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

@Task
public class InspiroTask extends AbstractTask {

	@Autowired
	private InspiroService inspiroService;
	
	@Override
	public TaskType getTaskType() {
		return TaskType.INSPIRO_DAILY_MESSAGE;
	}

	@Override
	public void executeInternal(JSONObject args, JDA jda, String userID, String guildID) {
		String channelID = args.getString("channelID");
		TextChannel channel = jda.getGuildById(guildID).getTextChannelById(channelID);
		if (channel == null) return;
		
		String inspiroImageURLOpt = inspiroService.getInspiroImageURLOpt();
		MessageEmbed inspiroEmbed = new EmbedBuilder()//
				.setTitle("Today's Inspiration", inspiroImageURLOpt)//
				.setColor(Color.ORANGE)//
				.setImage(inspiroImageURLOpt)//
				.setFooter("Data from InspiroBot", InspiroService.INSPIRO_ICON)//
				.build();
		channel.sendMessageEmbeds(inspiroEmbed).queue();
		inspiroService.scheduleInspiro(guildID, channelID);
	}

}