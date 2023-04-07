package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CountingChannelRepository extends JpaRepository<CountingChannel, Long> {

    @Query("FROM CountingChannel WHERE ChannelID = :channelID")
    CountingChannel getCountingChannelByChannelId(String channelID);

    @Modifying
    @Transactional
    @Query("DELETE FROM CountingChannel WHERE ChannelID = :channelID")
    void deleteCountingChannelByChannelId(String channelID);
}