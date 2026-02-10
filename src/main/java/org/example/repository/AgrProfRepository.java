package org.example.repository;

import org.example.model.agr_prof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgrProfRepository extends JpaRepository<agr_prof, Long> {

    @Query("SELECT a FROM agr_prof a WHERE a.mandt = :client " +
            "AND a.profile IN :profiles")
    List<agr_prof> findByClientAndProfiles(
            @Param("client") String client,
            @Param("profiles") List<String> profiles
    );
}