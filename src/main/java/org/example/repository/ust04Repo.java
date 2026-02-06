package org.example.repository;
import org.example.model.ust04;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ust04Repo extends JpaRepository<ust04, Long> {
    List<ust04> findByProfileIn(Set<String> profiles);
}