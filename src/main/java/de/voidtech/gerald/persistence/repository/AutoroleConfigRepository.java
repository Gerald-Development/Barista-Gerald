package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.AutoroleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface AutoroleConfigRepository extends JpaRepository<AutoroleConfig, Long> {

    @Query("FROM AutoroleConfig WHERE serverID = :serverID")
    List<AutoroleConfig> getAutoroleConfigsByServerId(long serverID);

    @Query("FROM AutoroleConfig WHERE roleID = :roleID")
    AutoroleConfig getAutoroleConfigByRoleId(String roleID);

    @Modifying
    @Transactional
    @Query("DELETE FROM AutoroleConfig WHERE roleID = :roleID")
    void deleteAutoroleConfigByRoleId(String roleID);

    @Modifying
    @Transactional
    @Query("DELETE FROM AutoroleConfig WHERE serverID = :serverID")
    void deleteAutoroleConfigsByServerId(long serverID);


}