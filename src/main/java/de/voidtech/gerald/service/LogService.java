package main.java.de.voidtech.gerald.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.JDA;

@Service
public class LogService {
	
	@Autowired
	private GeraldConfig config;
	
	@Autowired
	private JDA jda;

	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static GeraldLogger Logger;
	
	@EventListener(ApplicationReadyEvent.class)
	public void initialise() {
		GeraldLogger.AvatarUrl = jda.getSelfUser().getAvatarUrl();
		GeraldLogger.WebhookURL = config.getLoggingUrl();
		GeraldLogger.BotName = jda.getSelfUser().getName();
		
		Logger = GetLogger(LogService.class.getSimpleName());
		
		alertProgramStarted();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> alertProgramShutdown(), "Shutdown Alert"));
	}	
	
	public static GeraldLogger GetLogger(String className) {
		return new GeraldLogger(className);
	}
	
	public void alertProgramStarted() {
		Logger.log(Level.INFO, "Barista started at " + formatter.format(new Date()));
	}
	
	public void alertProgramShutdown() {
		Logger.log(Level.WARNING, "Barista shutting down at " + formatter.format(new Date()));
	}
	
}