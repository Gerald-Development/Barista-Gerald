package main.java.de.voidtech.gerald.routines;

import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.service.ThreadManager;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractRoutine {
	@Autowired
	private ServerService serverService;

	@Autowired
	ThreadManager threadManager;
	
	private void runRoutineInThread(Message message) {
		Runnable routineThreadRunnable = new Runnable() {
			public void run() {
				executeInternal(message);
			}
		};
		threadManager.getThreadByName("T-Routine").execute(routineThreadRunnable);
	}

    public void run(Message message) {
		Server server = serverService.getServer(message.getGuild().getId());
		if (!server.getRoutineBlacklist().contains(getName())) {
			runRoutineInThread(message);
		}
    }

    public abstract void executeInternal(Message message);

    public abstract String getName();
    
    public abstract String getDescription();
    
	public abstract RoutineCategory getRoutineCategory();
    
    public abstract boolean allowsBotResponses();

    public abstract boolean canBeDisabled();

}