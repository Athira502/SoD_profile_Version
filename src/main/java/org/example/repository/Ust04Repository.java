package org.example.repository;

import org.example.model.ust04;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Ust04Repository extends JpaRepository<ust04, Long> {

    @Query("SELECT u FROM ust04 u WHERE u.mandt = :client " +
            "AND u.profile IN :profiles")
    List<ust04> findByClientAndProfiles(
            @Param("client") String client,
            @Param("profiles") List<String> profiles
    );
}