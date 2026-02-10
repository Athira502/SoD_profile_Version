package org.example.repository;

import org.example.model.ust10c;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Ust10cRepository extends JpaRepository<ust10c, Long> {

    @Query("SELECT DISTINCT u.compProfile FROM ust10c u " +
            "WHERE u.client = :client AND u.singProfile IN :singProfiles")
    List<String> findCompositeProfilesByClientAndSingleProfiles(
            @Param("client") String client,
            @Param("singProfiles") List<String> singProfiles
    );
}