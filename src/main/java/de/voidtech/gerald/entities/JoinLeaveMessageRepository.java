package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface JoinLeaveMessageRepository extends JpaRepository<JoinLeaveMessage, Long> {

    @Query("FROM JoinLeaveMessage WHERE ServerID = :serverID")
    JoinLeaveMessage getJoinLeaveMessageByServerId(long serverID);

    @Modifying
    @Transactional
    @Query("DELETE FROM JoinLeaveMessage WHERE ServerID = :serverID")
    void deleteByServerId(long serverID);
}
