package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.Tunnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface TunnelRepository extends JpaRepository<Tunnel, Long> {

    @Query("FROM Tunnel WHERE sourceChannelID = :channelID OR destChannelID = :channelID")
    Tunnel getTunnelBySingleChannelId(String channelID);

    @Modifying
    @Transactional
    @Query("DELETE FROM Tunnel WHERE sourceChannelID = :channelID OR destChannelID = :channelID")
    void deleteTunnel(String channelID);
}
