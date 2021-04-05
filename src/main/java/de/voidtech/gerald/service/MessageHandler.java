package main.java.de.voidtech.gerald.service;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.util.CustomCollectors;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;

@Service
public class MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    
    @Autowired
    private GeraldConfig config;
    
    @Autowired
    private ServerService serverService;
    
    @Autowired
    private List<AbstractCommand> commands;
    
    @Autowired
    private List<AbstractRoutine> routines;

    // ENTRY POINT FROM MESSAGE LISTENER
    public void handleMessage(Message message) {
    	
    	if (message.isWebhookMessage()) return;
        runMessageRoutines(message);
    	
    	if(message.getAuthor().isBot()) return;
        handleCommandOnDemand(message);
    }
    
    private void runMessageRoutines(Message message) {
        for (AbstractRoutine routine : routines) {
        	if (message.getAuthor().isBot()) {
        		if (routine.allowsBotResponses()) {
                	routine.run(message);
        		}
        	} else {
            	routine.run(message);
        	}
			LOGGER.log(Level.FINE, "Routine executed: " + routine.getClass().getName());
        }
    }

    private void handleCommandOnDemand(Message message) {
    	String prefix = getPrefix(message);
    	
		if (!shouldHandleAsCommand(prefix, message)) return;
		
        String messageContent = message.getContentRaw().substring(prefix.length());
        List<String> messageArray = Arrays.asList(messageContent.trim().split("\\s+"));

		AbstractCommand commandOpt = commands.stream()
				.filter(command -> command.getName().equals(messageArray.get(0).toLowerCase()))
				.collect(CustomCollectors.toSingleton());

        if (commandOpt == null) {
            LOGGER.log(Level.INFO, "Command not found: " + messageArray.get(0));
            return;
        }
        
        if (message.getChannel().getType() == ChannelType.PRIVATE && !commandOpt.isDMCapable()) {
        	message.getChannel().sendMessage("**You can only use this command in guilds!**").queue();
        	return;
        }
        
        if (commandOpt.requiresArguments() && messageArray.size() <= 1) {
        	message.getChannel().sendMessage("**This command needs arguments to work! See the help command for more details!**\n" + commandOpt.getUsage()).queue();
        	return;
        }

        commandOpt.run(message, messageArray.subList(1, messageArray.size()));

        LOGGER.log(Level.INFO, "Command executed: " + messageArray.get(0) + " - From " + message.getAuthor().getAsTag() + "- ID: " + message.getAuthor().getId());
    }
    
    private boolean shouldHandleAsCommand(String prefix, Message message)
    {
    	boolean result = true;
    	String messageRaw = message.getContentRaw();
    	
    	result &= messageRaw.startsWith(prefix);
    	result &= messageRaw.length() > prefix.length();
    	
    	return result;
    }
    
    private String getPrefix(Message message) {
    	if (message.getChannelType() == ChannelType.PRIVATE) {
    		return config.getDefaultPrefix();
    	}
    	String customPrefix = serverService.getServer(message.getGuild().getId()).getPrefix();
    	
    	if(customPrefix == null) return config.getDefaultPrefix();
    	else return customPrefix;
    }
}