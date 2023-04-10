package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface RiggedLengthRepository extends JpaRepository<RiggedLength, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM RiggedLength WHERE memberID = :memberID")
    void deleteByMemberID(String memberID);

    @Query("FROM RiggedLength where memberID = :memberID")
    RiggedLength getRiggedLengthByMemberID(String memberID);
}
