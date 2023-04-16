package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

    @Query("FROM ChatChannel WHERE ChannelID = :channelID")
    ChatChannel getChatChannelByChannelId(String channelID);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatChannel WHERE ChannelID = :channelID")
    void deleteChatChannelByChannelId(String channelID);
}
