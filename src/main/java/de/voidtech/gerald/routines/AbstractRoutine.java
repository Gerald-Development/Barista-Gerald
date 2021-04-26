package main.java.de.voidtech.gerald.routines;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.service.ThreadManager;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractRoutine {

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
    	runRoutineInThread(message);
    }    

    public abstract void executeInternal(Message message);

    public abstract String getName();
    
    public abstract String getDescription();
    
    public abstract boolean allowsBotResponses();

}