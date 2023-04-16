package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.ServerExperienceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ServerExperienceConfigRepository extends JpaRepository<ServerExperienceConfig, Long> {
    @Query("FROM ServerExperienceConfig WHERE serverID = :serverID")
    ServerExperienceConfig getServerExperienceConfig(long serverID);

    @Modifying
    @Transactional
    @Query("DELETE FROM ServerExperienceConfig WHERE serverID = :serverID")
    void deleteServerExperienceConfig(long serverID);
}
