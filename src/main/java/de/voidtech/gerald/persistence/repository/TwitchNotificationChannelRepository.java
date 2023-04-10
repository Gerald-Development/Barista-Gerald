package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.TwitchNotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TwitchNotificationChannelRepository extends JpaRepository<TwitchNotificationChannel, Long> {

    @Query("SELECT DISTINCT streamerName FROM TwitchNotificationChannel")
    List<String> getAllTwitchChannels();

    @Query("FROM TwitchNotificationChannel WHERE LOWER(streamerName) = LOWER(:streamerName)")
    List<TwitchNotificationChannel> getChannelsByStreamerName(String streamerName);

    @Query("FROM TwitchNotificationChannel WHERE streamerName = :streamerName AND serverID = :serverID")
    TwitchNotificationChannel getSubscriptionByStreamerNameAndServer(String streamerName, long serverID);

    @Modifying
    @Transactional
    @Query("DELETE FROM TwitchNotificationChannel WHERE channelID = :channelID AND streamerName = :streamerName")
    void deleteSubscriptionByChannelId(String channelID, String streamerName);

    @Modifying
    @Transactional
    @Query("DELETE FROM TwitchNotificationChannel WHERE serverID = :serverID AND streamerName = :streamerName")
    void deleteSubscriptionByServerId(long serverID, String streamerName);

    @Query("SELECT COUNT(*) FROM TwitchNotificationChannel WHERE streamerName = :streamerName")
    long getCountSubscribedToStreamer(String streamerName);

    @Query("FROM TwitchNotificationChannel WHERE serverID = :serverID")
    List<TwitchNotificationChannel> getAllSubscriptionsForServer(long serverID);
}
