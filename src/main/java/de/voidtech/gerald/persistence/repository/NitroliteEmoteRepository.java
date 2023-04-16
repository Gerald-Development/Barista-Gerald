package main.java.de.voidtech.gerald.persistence.repository;

import main.java.de.voidtech.gerald.persistence.entity.NitroliteEmote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NitroliteEmoteRepository extends JpaRepository<NitroliteEmote, Long> {

    @Query("FROM NitroliteEmote WHERE LOWER(name) LIKE :name")
    List<NitroliteEmote> getEmoteListWithSimilarNames(String name);

    @Query(value = "SELECT * FROM NitroliteEmote WHERE emoteID = :id LIMIT 1", nativeQuery = true)
    NitroliteEmote getEmoteByEmoteId(String id);

    @Query(value = "SELECT * FROM NitroliteEmote WHERE LOWER(name) = :name LIMIT 1", nativeQuery = true)
    NitroliteEmote getOneEmoteByName(String name);

    @Query("FROM NitroliteEmote WHERE name = :name AND emoteID = :id")
    NitroliteEmote getEmoteByNameAndID(String name, String id);
}
