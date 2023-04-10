package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StarboardMessageRepository extends JpaRepository<StarboardMessage, Long> {

    @Query("FROM StarboardMessage WHERE serverID = :serverID AND originMessageID = :messageID")
    StarboardMessage getMessageByIdAndServerID(long serverID, String messageID);
}
