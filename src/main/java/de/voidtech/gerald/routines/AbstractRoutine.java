package main.java.de.voidtech.gerald.routines;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractRoutine implements Runnable {
    private Message message;
    private EventWaiter waiter;

    public AbstractRoutine() {
    }

    public void initRoutine(Message message, EventWaiter waiter) {
        this.message = message;
        this.waiter = waiter;
    }

    public void run() {
        executeInternal(message);
    }

    public abstract void executeInternal(Message message);

    public abstract String getDescription();

    public void sendErrorOccurred() {
        // TODO: refine message(?)
        this.message.getChannel().sendMessageFormat("```An error has occurred while executing a service```");
    }

    public EventWaiter getEventWaiter() {
        return this.waiter;
    }
}
