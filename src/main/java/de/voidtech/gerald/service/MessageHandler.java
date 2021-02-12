package main.java.de.voidtech.gerald.service;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;

@Service
public class MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    
    @Autowired
    private GeraldConfig config;
    
    @Autowired
    private List<AbstractCommand> commands;
    
    @Autowired
    private List<AbstractRoutine> routines;

    // ENTRY POINT FROM MESSAGE LISTENER
    public void handleMessage(Message message) {
    	if(message.getAuthor().isBot()) return;
    	
        handleCommandOnDemand(message);
        runMessageRoutines(message);
    }
    
    private void runMessageRoutines(Message message) {
        for (AbstractRoutine routine : routines) {
        	
        	routine.run(message);
			LOGGER.log(Level.FINE, "Routine executed: " + routine.getClass().getName());
        }
    }

    private void handleCommandOnDemand(Message message) {
        if (!message.getContentRaw().startsWith(config.getDefaultPrefix())
                || message.getContentRaw().length() == config.getDefaultPrefix().length())
            return;

        String messageContent = message.getContentRaw().substring(config.getDefaultPrefix().length());
        List<String> messageArray = Arrays.asList(messageContent.split(" "));

        AbstractCommand commandOpt = null;
        
        for (AbstractCommand command : commands) {
			if(command.getName().equals(messageArray.get(0).toLowerCase()))
			{
				commandOpt = command;
				break;
			}
		}

        if (commandOpt == null) {
            LOGGER.log(Level.INFO, "Command not found: " + messageArray.get(0));
            return;
        }
        
        if (message.getChannel().getType() == ChannelType.PRIVATE && !commandOpt.isDMCapable()) {
        	message.getChannel().sendMessage("**You can only use this command in guilds!**").queue();
        	return;
        }
        
        if (commandOpt.requiresArguments() && messageArray.size() <= 1) {
        	message.getChannel().sendMessage("**This command needs arguments to work!**\n" + commandOpt.getUsage()).queue();
        	return;
        }

        commandOpt.run(message, messageArray.subList(1, messageArray.size()));

        LOGGER.log(Level.INFO, "Command executed: " + messageArray.get(0) + " - From " + message.getAuthor().getAsTag() + "- ID: " + message.getAuthor().getId());
    }
}