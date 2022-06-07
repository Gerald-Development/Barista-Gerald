package main.java.de.voidtech.gerald.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.fun.InspiroCommand;
import main.java.de.voidtech.gerald.entities.DelayedTask;
import main.java.de.voidtech.gerald.entities.GeraldLogger;
import main.java.de.voidtech.gerald.tasks.TaskType;

@Service
public class InspiroService {
	
	public static final String REQUEST_URL = "https://inspirobot.me/api?generate=true";
	public static final String INSPIRO_ICON = "https://inspirobot.me/website/images/inspirobot-dark-green.png";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";
	
	private static final GeraldLogger LOGGER = LogService.GetLogger(InspiroCommand.class.getSimpleName());
	private static final long ONE_DAY_SECONDS = 86400;

	@Autowired
	private DelayedTaskService taskService;
	
	public String getInspiroImageURLOpt() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(REQUEST_URL).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					return in.lines().collect(Collectors.joining());
				}
			}
			con.disconnect();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + e.getMessage());
		}
		return null;
	}
	
	public void scheduleInspiro(String guildID, String channelID) {
		JSONObject args = new JSONObject().put("channelID", channelID);
		DelayedTask task = new DelayedTask(TaskType.INSPIRO_DAILY_MESSAGE, args, guildID, null,
				Instant.now().getEpochSecond() + ONE_DAY_SECONDS);
		taskService.saveDelayedTask(task);
	}
	
	private DelayedTask getGuildInspiroTask(String guildID) {
		List<DelayedTask> tasks = taskService.getGuildTasks(guildID, TaskType.INSPIRO_DAILY_MESSAGE);
		return tasks.isEmpty() ? null : tasks.get(0);
	}

	public boolean disableDaily(String guildID) {
		DelayedTask inspiroTask = getGuildInspiroTask(guildID);
		if (inspiroTask == null) return false;
		taskService.deleteTask(inspiroTask);
		return true;
	}
}