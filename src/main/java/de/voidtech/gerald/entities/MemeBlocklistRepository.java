package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemeBlocklistRepository extends JpaRepository<MemeBlocklist, Long> {

    @Query("FROM MemeBlocklist WHERE ServerID = :serverID")
    MemeBlocklist getBlocklist(long serverID);
}
