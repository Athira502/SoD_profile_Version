package org.example.repository;

import org.example.model.agr_define;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface AgrDefineRepository extends JpaRepository<agr_define, Long> {

    @Query("SELECT DISTINCT a.roleName FROM agr_define a " +
            "WHERE a.mandt = :client AND a.roleName IN :roleNames")
    Set<String> findExistingRoles(
            @Param("client") String client,
            @Param("roleNames") List<String> roleNames
    );

    @Query("SELECT COUNT(DISTINCT a.roleName) FROM agr_define a " +
            "WHERE a.mandt = :client AND a.roleName IN :roleNames")
    long countExistingRoles(
            @Param("client") String client,
            @Param("roleNames") List<String> roleNames
    );
}