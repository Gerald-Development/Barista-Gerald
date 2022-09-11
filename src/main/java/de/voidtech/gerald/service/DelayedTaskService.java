package main.java.de.voidtech.gerald.service;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.DelayedTask;
import main.java.de.voidtech.gerald.tasks.AbstractTask;
import main.java.de.voidtech.gerald.tasks.TaskType;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.JDA;

@Lazy
@Service
public class DelayedTaskService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
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
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();			
			session.saveOrUpdate(task);
			session.getTransaction().commit();
		}	
	}
	
	public void deleteTask(DelayedTask task) {
		try(Session session = sessionFactory.openSession())	{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM DelayedTask WHERE id = :id")
				.setParameter("id", task.getTaskID())
				.executeUpdate();
			session.getTransaction().commit();
		}
	}

	@SuppressWarnings("unchecked")
	private List<DelayedTask> getUpcomingTasks(long taskTimeThreshold) {
		try(Session session = sessionFactory.openSession())
		{
			return session.createQuery("FROM DelayedTask WHERE time < :timeThreshold")
                    .setParameter("timeThreshold", taskTimeThreshold)
                    .list();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<DelayedTask> getUserTasksOfType(String userID, TaskType type) {
		try(Session session = sessionFactory.openSession())
		{
			return session.createQuery("FROM DelayedTask WHERE userID = :userID AND type = :type")
                    .setParameter("userID", userID)
                    .setParameter("type", type.getType())
                    .list();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<DelayedTask> getGuildTasks(String guildID, TaskType type) {
		try(Session session = sessionFactory.openSession())
		{
			return session.createQuery("FROM DelayedTask WHERE guildID = :userID AND type = :type")
                    .setParameter("userID", guildID)
                    .setParameter("type", type.getType())
                    .list();
		}
	}
	
	public DelayedTask getTaskByID(long id) {
		try(Session session = sessionFactory.openSession())
		{
			return (DelayedTask) session.createQuery("FROM DelayedTask WHERE id = :id")
                    .setParameter("id", id)
                    .uniqueResult();
		}
	}

}
