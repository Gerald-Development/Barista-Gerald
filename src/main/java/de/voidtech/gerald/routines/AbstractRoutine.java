package main.java.de.voidtech.gerald.routines;

import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.MultithreadingService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractRoutine {
	@Autowired
	private ServerService serverService;

	@Autowired
	private MultithreadingService multithreadingService;

	private void runRoutineInThread(Message message) {
		Runnable routineThreadRunnable = () -> executeInternal(message);
		multithreadingService.getThreadByName("T-Routine").execute(routineThreadRunnable);
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