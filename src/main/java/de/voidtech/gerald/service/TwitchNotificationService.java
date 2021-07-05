package main.java.de.voidtech.gerald.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
	
	@Autowired	
	private TwitchClient twitchClient;
	
	@Bean
	public TwitchClient getTwitchClient() {
		String twitchClientId = geraldConfig.getTwitchClientId();
		String twitchClientSecret = geraldConfig.getTwitchSecret();
		return TwitchClientBuilder.builder()
				.withEnableHelix(true)
				.withClientId(twitchClientId)
				.withClientSecret(twitchClientSecret)
				.build();
	}
	
	public void subscribeToAllStreamers() {
		String twitchClientId = geraldConfig.getTwitchClientId();
		String twitchClientSecret = geraldConfig.getTwitchSecret();
		
		if (twitchClientId != null && twitchClientSecret != null) {
			LOGGER.log(Level.INFO, "Adding Twitch API subscriptions");
			
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
		if (notificationChannel == null)
			removeChannelSubscriptionByChannelId(channel.getStreamerName(), channel.getChannelId());
		else
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

	public void addSubscription(String streamerName, String channelId, String notificationMessage, long serverID) {
		if (zeroChannelsSubscribed(streamerName)) {
			twitchClient.getClientHelper().enableStreamEventListener(streamerName);
		}
		persistChannelSubscription(streamerName, channelId, notificationMessage, serverID);
	}

	private void persistChannelSubscription(String streamerName, String channelId, String notificationMessage, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			TwitchNotificationChannel channel = new TwitchNotificationChannel(channelId, streamerName, notificationMessage, serverID);
			session.saveOrUpdate(channel);
			session.getTransaction().commit();
		}
	}

	private boolean zeroChannelsSubscribed(String streamerName) {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("rawtypes")
			Query query = session.createQuery("SELECT COUNT(*) FROM TwitchNotificationChannel WHERE streamerName = :streamerName")
			.setParameter("streamerName", streamerName);
			long count = ((long)query.uniqueResult());
			session.close();
			return count == 0;
		}
	}

	public boolean subscriptionExists(String streamerName, long id) {
		try(Session session = sessionFactory.openSession())
		{
			TwitchNotificationChannel channel = (TwitchNotificationChannel) session.createQuery("FROM TwitchNotificationChannel WHERE streamerName = :streamerName AND serverID = :serverID")
					.setParameter("streamerName", streamerName)
					.setParameter("serverID", id)
                    .uniqueResult();
			return channel != null;
		}
	}

	private void removeChannelSubscriptionByChannelId(String streamerName, String id) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM TwitchNotificationChannel WHERE channelID = :channelID AND streamerName = :streamerName")
				.setParameter("channelID", id)
				.setParameter("streamerName", streamerName)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	public void removeChannelSubscription(String streamerName, long id) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM TwitchNotificationChannel WHERE serverID = :serverID AND streamerName = :streamerName")
				.setParameter("serverID", id)
				.setParameter("streamerName", streamerName)
				.executeUpdate();
			session.getTransaction().commit();
		}
		if (zeroChannelsSubscribed(streamerName)) {
			twitchClient.getClientHelper().disableStreamEventListener(streamerName);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<TwitchNotificationChannel> getAllSubscriptionsForServer(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			List<TwitchNotificationChannel> channels = (List<TwitchNotificationChannel>) session.createQuery("FROM TwitchNotificationChannel WHERE serverID = :serverID")
					.setParameter("serverID", serverID)
                    .list();
			return channels;
		}	
	}
}