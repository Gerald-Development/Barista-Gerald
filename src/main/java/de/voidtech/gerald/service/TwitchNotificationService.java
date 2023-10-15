package main.java.de.voidtech.gerald.service;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import main.java.de.voidtech.gerald.Gerald;
import main.java.de.voidtech.gerald.persistence.entity.TwitchNotificationChannel;
import main.java.de.voidtech.gerald.persistence.repository.TwitchNotificationChannelRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TwitchNotificationService {

    private static final Logger LOGGER = Logger.getLogger(TwitchNotificationService.class.getSimpleName());

    @Autowired
    private TwitchNotificationChannelRepository repository;

    @Autowired
    private JDA jda;

    private TwitchClient twitchClient;

    @Autowired
    public TwitchNotificationService(GeraldConfigService geraldConfig) {
        String twitchClientId = geraldConfig.getTwitchClientId();
        String twitchClientSecret = geraldConfig.getTwitchSecret();

        if (StringUtils.isBlank(twitchClientSecret) || StringUtils.isBlank(twitchClientId))
            LOGGER.log(Level.INFO, "No Twitch API credentials found, skipping Twitch Client instantiation.");
        else {
            LOGGER.log(Level.INFO, "Twitch Client creation with client: " + twitchClientId);

            this.twitchClient = TwitchClientBuilder.builder()
                    .withEnableHelix(true)
                    .withClientId(twitchClientId)
                    .withClientSecret(twitchClientSecret)
                    .build();

            LOGGER.log(Level.INFO, "Twitch Client has been initialised, hashcode: " + twitchClient.hashCode());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToAllStreamers() {
        if (twitchClient != null) {
            LOGGER.log(Level.INFO, "Adding Twitch API subscriptions");

            List<String> streamerNames = getAllStreamerNames();
            if (streamerNames != null) {
                for (String name : streamerNames) {
                    twitchClient.getClientHelper().enableStreamEventListener(name);
                }
            }

            LOGGER.log(Level.INFO, "Added Twitch API subscriptions!");
            listenForTwitchEvents();
        } else {
            LOGGER.log(Level.SEVERE, "No twitch client has been created, skipping Twitch subscriptions.");
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

    private List<String> getAllStreamerNames() {
        return repository.getAllTwitchChannels();
    }

    private List<TwitchNotificationChannel> getNotificationChannelsByStreamerName(String streamerName) {
        return repository.getChannelsByStreamerName(streamerName);
    }

    public void addSubscription(String streamerName, String channelId, String notificationMessage, long serverID) {
        if (zeroChannelsSubscribed(streamerName)) {
            twitchClient.getClientHelper().enableStreamEventListener(streamerName);
        }
        persistChannelSubscription(streamerName, channelId, notificationMessage, serverID);
    }

    private void persistChannelSubscription(String streamerName, String channelId, String notificationMessage, long serverID) {
        repository.save(new TwitchNotificationChannel(channelId, streamerName, notificationMessage, serverID));
    }

    private boolean zeroChannelsSubscribed(String streamerName) {
        return repository.getCountSubscribedToStreamer(streamerName) == 0;
    }

    public boolean subscriptionExists(String streamerName, long id) {
        return repository.getSubscriptionByStreamerNameAndServer(streamerName, id) != null;
    }

    private void removeChannelSubscriptionByChannelId(String streamerName, String id) {
        repository.deleteSubscriptionByChannelId(id, streamerName);
    }

    public void removeChannelSubscription(String streamerName, long id) {
        repository.deleteSubscriptionByServerId(id, streamerName);
        if (zeroChannelsSubscribed(streamerName)) {
            twitchClient.getClientHelper().disableStreamEventListener(streamerName);
        }
    }

    public List<TwitchNotificationChannel> getAllSubscriptionsForServer(long serverID) {
        return repository.getAllSubscriptionsForServer(serverID);
    }
}