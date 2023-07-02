package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.service.GeraldConfig;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ReadyListener implements EventListener {
	
	//Intentionally not using GeraldLogger
	private static final Logger LOGGER = Logger.getLogger(GeraldConfig.class.getName());
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof ReadyEvent) {
			String clientName = event.getJDA().getSelfUser().getEffectiveName();
			LOGGER.log(Level.INFO, "Coffee Machine is ready! Serving lattes as " + clientName);	
		}
	}
}