package main.java.de.voidtech.gerald.service;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandRegistry;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineRegistry;
import net.dv8tion.jda.api.entities.Message;

public class MessageHandler {

    private volatile static MessageHandler instance;
    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    private ConfigService config;
    public final String defaultPrefix;

    // private for Singleton
    private MessageHandler() {
        this.config = ConfigService.getInstance();
        this.defaultPrefix = config.getDefaultPrefix();
    }

    public static MessageHandler getInstance() {
        if (MessageHandler.instance == null) {
            MessageHandler.instance = new MessageHandler();
        }

        return MessageHandler.instance;
    }

    // ENTRY POINT FROM MESSAGE LISTENER
    public void handleMessage(Message message) {
    	
    	if(message.getAuthor().isBot()) return;
    	
    	EventWaiter waiter = getEventWaiter(message);
    	
        handleCommandOnDemand(message, waiter);
        runMessageRoutines(message, waiter);
    }
    
    private EventWaiter getEventWaiter(Message message)
    {
    	return message.getJDA()//
                .getRegisteredListeners()//
                .stream()//
                .filter(listener -> listener instanceof EventWaiter)//
                .map(listener -> (EventWaiter) listener)//
                .collect(Collectors.toList())//
                .get(0);
    }

    private void runMessageRoutines(Message message, EventWaiter waiter) {
        for (RoutineRegistry abstractRoutine : RoutineRegistry.values()) {
            try {
                AbstractRoutine routine = abstractRoutine.getRoutine();
                routine.initRoutine(message, waiter);

                Thread routineThread = new Thread(routine);
                routineThread.setName(routine.getClass().getName());
                routineThread.start();

                LOGGER.log(Level.FINE, "Routine executed: " + routine.getClass().getName());
            } catch (IllegalAccessException | InstantiationException e) {
                LOGGER.log(Level.SEVERE, "An Error has occurred while instantiating a Routine: " + e.getMessage());
            }
        }
    }

    private void handleCommandOnDemand(Message message, EventWaiter waiter) {
        if (!message.getContentRaw().startsWith(this.defaultPrefix)
                || message.getContentRaw().length() == this.defaultPrefix.length())
            return;

        String messageContent = message.getContentRaw().substring(this.defaultPrefix.length());
        List<String> messageArray = Arrays.asList(messageContent.split(" "));

        AbstractCommand commandOpt = getCommandByNameOpt(messageArray.get(0));

        if (commandOpt == null) {
            LOGGER.log(Level.INFO, "Command not found: " + messageArray.get(0));
            return;
        }

        commandOpt.initCommand(message, messageArray.subList(1, messageArray.size()), waiter);

        Thread commandThread = new Thread(commandOpt);
        commandThread.setName(commandOpt.getClass().getName());
        commandThread.start();

        LOGGER.log(Level.INFO, "Command executed: " + messageArray.get(0) + "\nfrom " + message.getAuthor().getAsTag()
                + "\nID: " + message.getAuthor().getId());
    }

    private AbstractCommand getCommandByNameOpt(String commandName) {
        try {
            for (CommandRegistry registryEntry : CommandRegistry.values()) {
                if (registryEntry.getName().equals(commandName.toLowerCase())) {
                    return registryEntry.getCommand();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An Error has occurred while instantiating a Command: " + e.getMessage());
        }

        return null;
    }
}