package main.java.de.voidtech.gerald.listeners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class ReadyListener implements EventListener {

	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof ReadyEvent)
			System.out.println("Coffee machine is ready!");
	}

}
