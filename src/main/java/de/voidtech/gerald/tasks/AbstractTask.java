package main.java.de.voidtech.gerald.tasks;

import main.java.de.voidtech.gerald.service.MultithreadingService;
import net.dv8tion.jda.api.JDA;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractTask {

    @Autowired
    private MultithreadingService multithreadingService;

    public void execute(JSONObject args, JDA jda, String userID, String guildID) {
        Runnable taskRunnable = () -> executeInternal(args, jda, userID, guildID);
        multithreadingService.getThreadByName("T-Task").execute(taskRunnable);
    }

    public abstract TaskType getTaskType();

    public abstract void executeInternal(JSONObject args, JDA jda, String userID, String guildID);
}
