package main.java.de.voidtech.gerald.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, Long> {

    @Query("FROM GlobalConfig")
    GlobalConfig getGlobalConfig();
}
