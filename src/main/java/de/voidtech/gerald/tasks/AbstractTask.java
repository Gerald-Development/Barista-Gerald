package main.java.de.voidtech.gerald.tasks;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.service.ThreadManager;
import net.dv8tion.jda.api.JDA;

public abstract class AbstractTask {
	
	@Autowired
	private ThreadManager threadManager;
	
	public void execute(JSONObject args, JDA jda, String userID, String guildID) {
		Runnable taskRunnable = () -> executeInternal(args, jda, userID, guildID);
		threadManager.getThreadByName("T-Task").execute(taskRunnable); 
	}
	
	public abstract TaskType getTaskType();
	public abstract void executeInternal(JSONObject args, JDA jda, String userID, String guildID);
}
