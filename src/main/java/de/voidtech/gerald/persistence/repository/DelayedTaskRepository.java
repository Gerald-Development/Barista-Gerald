package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.DelayedTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface DelayedTaskRepository extends JpaRepository<DelayedTask, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DelayedTask WHERE id = :id")
    void deleteTaskById(long id);

    @Query("FROM DelayedTask WHERE time < :timeThreshold")
    List<DelayedTask> getUpcomingtasks(long timeThreshold);

    @Query("FROM DelayedTask WHERE userID = :userID AND type = :type")
    List<DelayedTask> getTasksByUserIdAndTaskType(String userID, String type);

    @Query("FROM DelayedTask WHERE guildID = :guildID AND type = :type")
    List<DelayedTask> getTasksByGuildIdAndTaskType(String guildID, String type);

    @Query("FROM DelayedTask WHERE id = :id")
    DelayedTask getTaskById(long id);
}
