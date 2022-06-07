package main.java.de.voidtech.gerald.service;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.DelayedTask;
import main.java.de.voidtech.gerald.tasks.TaskType;
import net.dv8tion.jda.api.entities.Guild;

@Service
public class RemindMeService {

	@Autowired
	private DelayedTaskService taskService;
	
	public void addReminder(CommandContext context, String message, long time) {
		DelayedTask task = new DelayedTask(TaskType.REMIND_ME,
				new JSONObject().put("channelID", context.getChannel().getId()).put("message", message),
				context.getGuild().getId(),
				context.getAuthor().getId(),
				time);		
		taskService.saveDelayedTask(task);
	}
	
	public String getRemindersList(CommandContext context) {
		List<DelayedTask> tasks = taskService.getUserTasksOfType(context.getAuthor().getId(), TaskType.REMIND_ME);
		
		String list = "";
		
		for (DelayedTask task : tasks) {
			Guild guild = context.getJDA().getGuildById(task.getGuildID());
			if (guild == null) {
				taskService.deleteTask(task);
			} else {
				list += "`" + task.getTaskID() +  "` **" + guild.getName() + "** -  <t:" + task.getExecutionTime() + "> - ";
				list += formatMessage(task.getArgs().getString("message"));	
				list += "\n";
			}
		}
		return list == "" ? "**No reminders!**" : list;
	}

	private String formatMessage(String msg) {
		return msg.length() > 20 ? msg.substring(0, 20) + "..." : msg;
	}
	
	public boolean deleteReminder(String userID, long id) {
		DelayedTask task = taskService.getTaskByID(id);
		if (task == null) return false;
		if (!task.getUserID().equals(userID)) return false;
		taskService.deleteTask(task);
		return true;
	}

}