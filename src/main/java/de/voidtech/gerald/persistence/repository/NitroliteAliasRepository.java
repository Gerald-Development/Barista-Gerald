package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.NitroliteAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface NitroliteAliasRepository extends JpaRepository<NitroliteAlias, Long> {

    @Query("FROM NitroliteAlias WHERE ServerID = :serverID AND aliasName = :aliasName")
    NitroliteAlias getAliasByNameAndServerID(long serverID, String aliasName);

    @Query("FROM NitroliteAlias WHERE ServerID = :serverID")
    List<NitroliteAlias> getAllAliasesByServerID(long serverID);

    @Modifying
    @Transactional
    @Query("DELETE FROM NitroliteAlias WHERE ServerID = :serverID AND AliasName = :aliasName")
    void deleteAliasByNameAndServerID(long serverID, String aliasName);

    @Modifying
    @Transactional
    @Query("DELETE FROM NitroliteAlias WHERE emoteID = :emoteID")
    void deleteAliasByEmoteID(String emoteID);
}
