
package org.example.repository;
import org.example.model.agr_1251;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;
@Repository
public interface agr_1251Repo extends JpaRepository<agr_1251, Long> {
    @Query("SELECT a FROM agr_1251 a WHERE a.object IN :objects")
    List<agr_1251> findByObjects(@Param("objects") Set<String> objects);
}