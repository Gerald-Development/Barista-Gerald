package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MessageService {

    private static final Logger LOGGER = Logger.getLogger(MessageService.class.getSimpleName());

    @Autowired
    private List<AbstractRoutine> routines;

    @Autowired
    private CommandService cmdService;

    public void handleMessage(Message message) {

        if (message.isWebhookMessage()) return;
        runMessageRoutines(message);

        if (message.getAuthor().isBot()) return;
        cmdService.handleChatCommandOnDemand(message);
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
