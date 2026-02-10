package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.Ust10cRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileHierarchyService {

    private final Ust10cRepository ust10cRepository;

    /**
     * Expand profiles to include all composite (parent) profiles
     */
    public Set<String> expandCompositeProfiles(String clientId, Set<String> profiles) {
        Set<String> allProfiles = new HashSet<>(profiles);
        Set<String> currentLevel = new HashSet<>(profiles);
        Set<String> processedProfiles = new HashSet<>();

        int iteration = 0;
        final int MAX_ITERATIONS = 50; // Prevent infinite loops

        while (!currentLevel.isEmpty() && iteration < MAX_ITERATIONS) {
            iteration++;
            log.debug("Profile expansion iteration {}: processing {} profiles",
                    iteration, currentLevel.size());

            // Find parent profiles for current level
            List<String> parentProfiles = ust10cRepository
                    .findCompositeProfilesByClientAndSingleProfiles(
                            clientId, new ArrayList<>(currentLevel)
                    );

            if (parentProfiles.isEmpty()) {
                log.debug("No more parent profiles found at iteration {}", iteration);
                break;
            }

            // Add new parent profiles
            Set<String> newParents = new HashSet<>(parentProfiles);
            newParents.removeAll(allProfiles); // Only keep truly new ones

            if (newParents.isEmpty()) {
                log.debug("All parent profiles already processed");
                break;
            }

            allProfiles.addAll(newParents);
            processedProfiles.addAll(currentLevel);

            // Next iteration: process only the new parents
            currentLevel = newParents;

            log.debug("Added {} new parent profiles", newParents.size());
        }

        if (iteration >= MAX_ITERATIONS) {
            log.warn("Profile expansion reached maximum iterations ({})", MAX_ITERATIONS);
        }

        log.info("Profile expansion complete: {} -> {} profiles",
                profiles.size(), allProfiles.size());

        return allProfiles;
    }
}