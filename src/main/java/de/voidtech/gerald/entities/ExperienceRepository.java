package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    @Query("FROM Experience WHERE userID = :userID AND serverID = :serverID")
    Experience getUserExperience(String userID, long serverID);

    @Query("FROM Experience WHERE serverID = :serverID ORDER BY totalExperience DESC")
    List<Experience> getServerLeaderboard(long serverID);

    @Query(value = "SELECT * FROM Experience WHERE serverID = :serverID ORDER BY total_experience DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Experience> getLeaderboardChunk(long serverID, long limit, long offset);

    @Modifying
    @Transactional
    @Query("DELETE FROM Experience WHERE serverID = :serverID")
    void deleteAllExperienceForServer(long serverID);
}
