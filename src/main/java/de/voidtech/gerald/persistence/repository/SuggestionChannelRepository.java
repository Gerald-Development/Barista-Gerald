package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.SuggestionChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface SuggestionChannelRepository extends JpaRepository<SuggestionChannel, Long> {

    @Query("FROM SuggestionChannel WHERE ServerID = :serverID")
    SuggestionChannel getChannelByServerId(long serverID);

    @Modifying
    @Transactional
    @Query("DELETE FROM SuggestionChannel WHERE ServerID = :guildID")
    void deleteSuggestionChannel(long guildID);
}
