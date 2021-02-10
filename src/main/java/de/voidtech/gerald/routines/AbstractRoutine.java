package main.java.de.voidtech.gerald.routines;

import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractRoutine {

    public void run(Message message) {
        executeInternal(message);
    }

    public abstract void executeInternal(Message message);

    public abstract String getDescription();

}