package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionStatsRepository extends JpaRepository<ActionStats, Long> {

    @Query("FROM ActionStats WHERE memberID = :member AND type = :type AND serverID = :serverID")
    ActionStats getActionStatsProfile(String memberID, String type, long serverID);

    @Query("FROM ActionStats WHERE type = :type AND serverID = :serverID AND givenCount > 0 ORDER BY givenCount DESC")
    List<ActionStats> getTopGivenByTypeInServer(String type, long serverID);

    @Query("FROM ActionStats WHERE type = :type AND serverID = :serverID AND receivedCount > 0 ORDER BY receivedCount DESC")
    List<ActionStats> getTopReceivedByTypeInServer(String type, long serverID);
}
