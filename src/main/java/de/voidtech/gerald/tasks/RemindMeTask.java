package main.java.de.voidtech.gerald.tasks;

import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Task;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

@Task
public class RemindMeTask extends AbstractTask {
	
	@Override
	public void executeInternal(JSONObject args, JDA jda, String userID, String guildID) {
		String channelID = args.getString("channelID");
		String message = "**Reminder for** <@" + userID + "> - " + args.getString("message");
		
		Guild guild = jda.getGuildById(guildID);
		if (guild == null) return;
		TextChannel channel = guild.getTextChannelById(channelID);
		if (channel == null) return;
		
		channel.sendMessage(message).queue();
	}

	@Override
	public TaskType getTaskType() {
		return TaskType.REMIND_ME;
	}

}
