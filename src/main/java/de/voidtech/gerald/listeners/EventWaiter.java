package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listener
public class EventWaiter extends ListenerAdapter {
    private final HashMap<Class<?>, Set<WaitingEvent>> waitingEvents;
    private final ScheduledExecutorService threadpool;
    private final boolean shutdownAutomatically;

    public EventWaiter() {
        this(Executors.newSingleThreadScheduledExecutor(), true);
    }

    public EventWaiter(ScheduledExecutorService threadpool, boolean shutdownAutomatically) {
        Checks.notNull(threadpool, "ScheduledExecutorService");
        Checks.check(!threadpool.isShutdown(), "Cannot construct EventWaiter with a closed ScheduledExecutorService!");
        this.waitingEvents = new HashMap<>();
        this.threadpool = threadpool;
        this.shutdownAutomatically = shutdownAutomatically;
    }

    public boolean isShutdown() {
        return this.threadpool.isShutdown();
    }

    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action, long timeout, TimeUnit unit, Runnable timeoutAction) {
        Checks.check(!this.isShutdown(), "Attempted to register a WaitingEvent while the EventWaiter's threadpool was already shut down!");
        Checks.notNull(classType, "The provided class type");
        Checks.notNull(condition, "The provided condition predicate");
        Checks.notNull(action, "The provided action consumer");
        WaitingEvent we = new WaitingEvent(condition, action);
        Set<WaitingEvent> set = this.waitingEvents.computeIfAbsent(classType, (c) -> new HashSet<>());
        set.add(we);
        if (timeout > 0L && unit != null) {
            this.threadpool.schedule(() -> {
                if (set.remove(we) && timeoutAction != null) {
                    timeoutAction.run();
                }

            }, timeout, unit);
        }

    }

    @Override
    public final void onGenericEvent(GenericEvent event) {
        for (Class c = event.getClass(); c != null; c = c.getSuperclass()) {
            if (this.waitingEvents.containsKey(c)) {
                Set<WaitingEvent> set = (Set) this.waitingEvents.get(c);
                WaitingEvent[] toRemove = set.toArray(new WaitingEvent[set.size()]);
                set.removeAll(Stream.of(toRemove).filter((i) -> {
                    return i.attempt(event);
                }).collect(Collectors.toSet()));
            }

            if (event instanceof ShutdownEvent && this.shutdownAutomatically) {
                this.threadpool.shutdown();
            }
        }

    }

    private static class WaitingEvent<T extends GenericEvent> {
        final Predicate<T> condition;
        final Consumer<T> action;

        WaitingEvent(Predicate<T> condition, Consumer<T> action) {
            this.condition = condition;
            this.action = action;
        }

        boolean attempt(T event) {
            if (this.condition.test(event)) {
                this.action.accept(event);
                return true;
            } else {
                return false;
            }
        }
    }
}
