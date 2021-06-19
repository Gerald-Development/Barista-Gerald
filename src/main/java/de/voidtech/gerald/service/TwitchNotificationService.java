package main.java.de.voidtech.gerald.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;

@Service
public class TwitchNotificationService {
	
	private static final Logger LOGGER = Logger.getLogger(TwitchNotificationService.class.getName());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private GeraldConfig geraldConfig;
	
	private TwitchClient twitchClient;
	
	public void subscribeToAllStreamers() {
		LOGGER.log(Level.INFO, "Adding Twitch API subscriptions");
		
		twitchClient = TwitchClientBuilder.builder()
				.withEnableHelix(true)
				.withClientId(geraldConfig.getTwitchClientId())
				.withClientSecret(geraldConfig.getTwitchSecret())
				.build();
		
		List<String> streamerNames = getAllStreamerNames();
		if (streamerNames != null) {
			for (String name : streamerNames) {
				twitchClient.getClientHelper().enableStreamEventListener(name);
			}
		}
		
		LOGGER.log(Level.INFO, "Added Twitch API subscriptions!");
	}

	@SuppressWarnings("unchecked")
	private List<String> getAllStreamerNames() {
		try(Session session = sessionFactory.openSession())
		{
			List<String> names = (List<String>) session.createQuery("SELECT DISTINCT streamerName FROM TwitchNotificationChannel")
                    .list();
			return names;
		}	
	}
	
}
