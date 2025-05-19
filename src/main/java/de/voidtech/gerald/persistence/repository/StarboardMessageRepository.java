package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.StarboardMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarboardMessageRepository extends JpaRepository<StarboardMessage, Long> {

    @Query("FROM StarboardMessage WHERE serverID = :serverID AND originMessageID = :messageID")
    StarboardMessage getMessageByIdAndServerID(long serverID, String messageID);

    @Query("FROM StarboardMessage WHERE serverID = :serverId")
    List<StarboardMessage> getAllMessagesByServerId(long serverId);

}
