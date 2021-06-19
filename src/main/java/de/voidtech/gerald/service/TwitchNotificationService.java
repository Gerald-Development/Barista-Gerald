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
import com.github.twitch4j.events.ChannelGoLiveEvent;

import main.java.de.voidtech.gerald.entities.TwitchNotificationChannel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

@Service
public class TwitchNotificationService {
	
	private static final Logger LOGGER = Logger.getLogger(TwitchNotificationService.class.getName());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private GeraldConfig geraldConfig;
	
	@Autowired
	private JDA jda;
	
	private TwitchClient twitchClient;
	
	public void subscribeToAllStreamers() {
		String twitchClientId = geraldConfig.getTwitchClientId();
		String twitchClientSecret = geraldConfig.getTwitchSecret();
		
		if (twitchClientId != null && twitchClientSecret != null) {
			LOGGER.log(Level.INFO, "Adding Twitch API subscriptions");
			twitchClient = TwitchClientBuilder.builder()
					.withEnableHelix(true)
					.withClientId(twitchClientId)
					.withClientSecret(twitchClientSecret)
					.build();
			
			List<String> streamerNames = getAllStreamerNames();
			if (streamerNames != null) {
				for (String name : streamerNames) {
					twitchClient.getClientHelper().enableStreamEventListener(name);
				}
			}
			
			LOGGER.log(Level.INFO, "Added Twitch API subscriptions!");
			listenForTwitchEvents();
		}
	}

	private void listenForTwitchEvents() {
		twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {
			String streamerName = event.getChannel().getName();
			List<TwitchNotificationChannel> channels = getNotificationChannelsByStreamerName(streamerName);
			for (TwitchNotificationChannel channel : channels) {
				sendNotificationMessage(channel, jda);
			}
		});
	}

	private void sendNotificationMessage(TwitchNotificationChannel channel, JDA jda) {
		TextChannel notificationChannel = jda.getTextChannelById(channel.getChannelId());
		notificationChannel.sendMessage(formMessage(channel)).queue();
	}

	private CharSequence formMessage(TwitchNotificationChannel channel) {
		return channel.getNotificationMessage() + "\nhttps://twitch.tv/" + channel.getStreamerName();
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
	
	@SuppressWarnings("unchecked")
	private List<TwitchNotificationChannel> getNotificationChannelsByStreamerName(String streamerName) {
		try(Session session = sessionFactory.openSession())
		{
			List<TwitchNotificationChannel> channels = (List<TwitchNotificationChannel>) session.createQuery("FROM TwitchNotificationChannel WHERE streamerName = :streamerName")
					.setParameter("streamerName", streamerName)
                    .list();
			return channels;
		}	
	}
	
}
