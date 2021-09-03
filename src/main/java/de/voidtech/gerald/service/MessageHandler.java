package main.java.de.voidtech.gerald.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;

@Service
public class MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    
    private static final int LEVENSHTEIN_THRESHOLD = 1;
    
    @Autowired
    private List<AbstractRoutine> routines;
    
	@Autowired
	private CommandService cmdService;
    
    public void handleMessage(Message message) {
    	
    	if (message.isWebhookMessage()) return;
        runMessageRoutines(message);
    	
    	if(message.getAuthor().isBot()) return;
        cmdService.handleCommandOnDemand(message);
    }
    
    private void runMessageRoutines(Message message) {
    	if (message.getChannel().getType().equals(ChannelType.PRIVATE)) return;
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


}