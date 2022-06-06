package main.java.de.voidtech.gerald.listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import main.java.de.voidtech.gerald.service.GeraldConfig;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class ReadyListener implements EventListener {
	
	//Intentionally not using GeraldLogger
	private static final Logger LOGGER = Logger.getLogger(GeraldConfig.class.getName());
	
	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof ReadyEvent) {
			String clientName = event.getJDA().getSelfUser().getAsTag();
			LOGGER.log(Level.INFO, "Coffee Machine is ready! Serving lattes as " + clientName);	
		}
	}
}
