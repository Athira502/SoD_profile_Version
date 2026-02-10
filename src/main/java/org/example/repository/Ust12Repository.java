package org.example.repository;

import org.example.model.ust12;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Ust12Repository extends JpaRepository<ust12, Long> {

    @Query("SELECT u FROM ust12 u WHERE u.client = :client " +
            "AND u.authObj = :authObj AND u.authField = :field")
    List<ust12> findByClientAndAuthObjAndField(
            @Param("client") String client,
            @Param("authObj") String authObj,
            @Param("field") String field
    );

    @Query("SELECT DISTINCT u.usermasterMaint FROM ust12 u " +
            "WHERE u.client = :client AND u.usermasterMaint IN :usermasterMaints")
    List<String> findDistinctUsermasterMaintByClientAndUsermasterMaintIn(
            @Param("client") String client,
            @Param("usermasterMaints") List<String> usermasterMaints
    );
}