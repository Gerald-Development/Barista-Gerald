package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.StarboardConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface StarboardConfigRepository extends JpaRepository<StarboardConfig, Long> {

    @Query("FROM StarboardConfig WHERE ServerID = :serverID")
    StarboardConfig getConfigByServerID(long serverID);

    @Modifying
    @Transactional
    @Query("DELETE FROM StarboardConfig WHERE ServerID = :serverID")
    void deleteConfigByServerID(long serverID);

    @Query("FROM StarboardConfig WHERE serverID = :serverID AND starboardChannel = :channelID")
    StarboardConfig getConfigIfServerIdAndChannelIdMatch(long serverID, String channelID);
}
