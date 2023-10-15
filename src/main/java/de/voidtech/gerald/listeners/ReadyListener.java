package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.annotations.Listener;
import main.java.de.voidtech.gerald.service.AlarmSenderService;
import main.java.de.voidtech.gerald.service.GeraldConfigService;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Level;
import java.util.logging.Logger;

@Listener
public class ReadyListener extends ListenerAdapter {

    private static final Logger LOGGER = Logger.getLogger(GeraldConfigService.class.getName());

    @Autowired
    private AlarmSenderService alarmSenderService;

    @Override
    public void onReady(ReadyEvent event) {
        String clientName = event.getJDA().getSelfUser().getEffectiveName();
        alarmSenderService.ready(event.getJDA());
        LOGGER.log(Level.INFO, "Coffee Machine is ready! Serving lattes as " + clientName);
    }
}