package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface LevelUpRoleRepository extends JpaRepository<LevelUpRole, Long> {

    @Query("FROM LevelUpRole WHERE serverID = :serverID AND level <= :level")
    List<LevelUpRole> getLevelUpRolesInServerForLevel(long serverID, long level);

    @Query("FROM LevelUpRole WHERE serverID = :serverID")
    List<LevelUpRole> getAllLevelUpRolesForServer(long serverID);

    @Query("FROM LevelUpRole WHERE serverID = :serverID AND level = :level")
    LevelUpRole getLevelUpRoleForLevelInServer(long serverID, long level);

    @Modifying
    @Transactional
    @Query("DELETE FROM LevelUpRole WHERE level = :level AND serverID = :serverID")
    void deleteRoleFromServer(long level, long serverID);

    @Modifying
    @Transactional
    @Query("DELETE FROM LevelUpRole WHERE serverID = :serverID")
    void deleteAllRolesFromServer(long serverID);

}
