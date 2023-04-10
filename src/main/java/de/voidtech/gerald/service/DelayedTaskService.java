package main.java.de.voidtech.gerald.service;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import main.java.de.voidtech.gerald.persistence.repository.DelayedTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.persistence.entity.DelayedTask;
import main.java.de.voidtech.gerald.tasks.AbstractTask;
import main.java.de.voidtech.gerald.tasks.TaskType;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.JDA;

@Lazy
@Service
public class DelayedTaskService {
	
	@Autowired
	private DelayedTaskRepository repository;
	
	@Autowired
	private List<AbstractTask> abstractTasks;
	
	@Lazy 
	@Autowired
	private JDA jda;
	
	//Time in milliseconds
	private static final int DELAYED_TASK_TIMER_INTERVAL = 10000;
	//Time in seconds
	private static final int UPCOMING_TASKS_TIME = 10;
	
	private static final GeraldLogger LOGGER = LogService.GetLogger(DelayedTaskService.class.getSimpleName());
	
	@EventListener(ApplicationReadyEvent.class)
	private void startTimer() {
		TimerTask checkTaskTimer = new TimerTask() {
		    public void run() {
		    	Thread.currentThread().setName("Task Timer"); 
		    	checkForTasks();
		    }
		 };
		
		Timer timer = new Timer();
		timer.schedule(checkTaskTimer, DELAYED_TASK_TIMER_INTERVAL, DELAYED_TASK_TIMER_INTERVAL);
	}
	
	private void checkForTasks() {
		long taskTimeThreshold = Instant.now().getEpochSecond() + UPCOMING_TASKS_TIME;
		List<DelayedTask> tasks = getUpcomingTasks(taskTimeThreshold);
		if (tasks.isEmpty()) return;
		for (DelayedTask task : tasks) {
			AbstractTask taskExecutor = abstractTasks.stream()
					.filter(t -> t.getTaskType().getType().equals(task.getTaskType()))
					.findFirst()
					.orElse(null);
			if (taskExecutor == null) LOGGER.log(Level.SEVERE, "Task with unknown type '" + task.getTaskType() + "' found");
			else taskExecutor.execute(task.getArgs(), jda, task.getUserID(), task.getGuildID());
			
			deleteTask(task);
		}
	}
	
	public void saveDelayedTask(DelayedTask task) {
		repository.save(task);
	}
	
	public void deleteTask(DelayedTask task) {
		repository.deleteTaskById(task.getTaskID());
	}

	private List<DelayedTask> getUpcomingTasks(long taskTimeThreshold) {
		return repository.getUpcomingtasks(taskTimeThreshold);
	}

	public List<DelayedTask> getUserTasksOfType(String userID, TaskType type) {
		return repository.getTasksByUserIdAndTaskType(userID, type.getType());
	}

	public List<DelayedTask> getGuildTasks(String guildID, TaskType type) {
		return repository.getTasksByGuildIdAndTaskType(guildID, type.getType());
	}
	
	public DelayedTask getTaskByID(long id) {
		return repository.getTaskById(id);
	}
}