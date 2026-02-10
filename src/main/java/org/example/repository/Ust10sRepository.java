package org.example.repository;

import org.example.model.ust10s;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Ust10sRepository extends JpaRepository<ust10s, Long> {

    @Query("SELECT DISTINCT u.profile FROM ust10s u " +
            "WHERE u.client = :client AND u.usermasterMaint IN :usermasterMaints")
    List<String> findProfilesByClientAndUsermasterMaints(
            @Param("client") String client,
            @Param("usermasterMaints") List<String> usermasterMaints
    );
}