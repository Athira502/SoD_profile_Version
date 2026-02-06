package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.*;
import org.example.model.*;
import org.example.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAnalysisService {

    private final agr_1251Repo agrRepository;
    private final ust04Repo ustRepository;

    public AnalysisResult analyze(RiskRequest request) {
        log.info("Starting risk analysis for: {}", request.getRiskId());


        String functionLogic = request.getLogic() != null ? request.getLogic() : "AND";


        List<Set<String>> functionRoleSets = new ArrayList<>();

        for (FunctionRule function : request.getFunctions()) {
            Set<String> rolesForFunction = evaluateFunction(function);
            functionRoleSets.add(rolesForFunction);
            log.info("Function {} satisfied by {} roles", function.getFunctionId(), rolesForFunction.size());
        }

        Set<String> criticalRoles;
        if ("AND".equalsIgnoreCase(functionLogic)) {
            criticalRoles = functionRoleSets.stream()
                    .reduce((set1, set2) -> {
                        Set<String> intersection = new HashSet<>(set1);
                        intersection.retainAll(set2);
                        return intersection;
                    })
                    .orElse(new HashSet<>());
        } else {
            // Union - roles must satisfy ANY function
            criticalRoles = functionRoleSets.stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        }

        log.info("Found {} critical roles after applying {} logic", criticalRoles.size(), functionLogic);

        // Step 3: Get all permissions for critical roles to extract profiles
        Set<String> allObjects = request.getFunctions().stream()
                .flatMap(f -> f.getCriteria().stream())
                .map(Criteria::getObject)
                .collect(Collectors.toSet());

        List<agr_1251> allPermissions = agrRepository.findByObjects(allObjects);

        Map<String, List<agr_1251>> permissionsByRole = allPermissions.stream()
                .filter(p -> criticalRoles.contains(p.getAgrName()))
                .collect(Collectors.groupingBy(agr_1251::getAgrName));


        List<RoleLevelOutput> roleOutputs = new ArrayList<>();
        Map<String, Set<String>> profileToRolesMap = new HashMap<>();

        for (String roleName : criticalRoles) {
            roleOutputs.add(new RoleLevelOutput(
                    request.getRiskId(),
                    request.getDescription(),
                    roleName
            ));

            List<agr_1251> rolePerms = permissionsByRole.getOrDefault(roleName, Collections.emptyList());
            Set<String> profiles = extractProfiles(rolePerms);

            for (String profile : profiles) {
                profileToRolesMap.computeIfAbsent(profile, k -> new HashSet<>()).add(roleName);
            }
        }

        // Step 5: Find users with these profiles
        List<UserLevelOutput> userOutputs = findUsersWithProfiles(request, profileToRolesMap);

        log.info("Found {} users with critical access", userOutputs.size());

        return new AnalysisResult(roleOutputs, userOutputs);
    }


    private Set<String> evaluateFunction(FunctionRule function) {

        List<List<Criteria>> criteriaGroups = parseCriteriaGroups(function.getCriteria());

        // Get all relevant permissions
        Set<String> objects = function.getCriteria().stream()
                .map(Criteria::getObject)
                .collect(Collectors.toSet());

        List<agr_1251> allPermissions = agrRepository.findByObjects(objects);

        // Group by role
        Map<String, List<agr_1251>> permissionsByRole = allPermissions.stream()
                .collect(Collectors.groupingBy(agr_1251::getAgrName));

        // Evaluate each role
        Set<String> qualifyingRoles = new HashSet<>();

        for (Map.Entry<String, List<agr_1251>> entry : permissionsByRole.entrySet()) {
            String roleName = entry.getKey();
            List<agr_1251> rolePermissions = entry.getValue();

            if (doesRoleSatisfyFunction(criteriaGroups, rolePermissions)) {
                qualifyingRoles.add(roleName);
            }
        }

        return qualifyingRoles;
    }


    private List<List<Criteria>> parseCriteriaGroups(List<Criteria> criteria) {
        List<List<Criteria>> groups = new ArrayList<>();
        List<Criteria> currentGroup = new ArrayList<>();

        for (int i = 0; i < criteria.size(); i++) {
            Criteria crit = criteria.get(i);
            currentGroup.add(crit);

            if (i == criteria.size() - 1 || "AND".equalsIgnoreCase(crit.getOperator())) {
                groups.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
            }
        }

        return groups;
    }


    private boolean doesRoleSatisfyFunction(List<List<Criteria>> criteriaGroups, List<agr_1251> rolePermissions) {
        // Group permissions by AUTH (authorization object instance)
        Map<String, List<agr_1251>> permsByAuth = rolePermissions.stream()
                .filter(p -> p.getAuth() != null && !p.getAuth().isEmpty())
                .collect(Collectors.groupingBy(agr_1251::getAuth));

        // Check if ANY auth group satisfies ALL criteria groups
        return permsByAuth.values().stream()
                .anyMatch(authGroup -> {
                    // ALL criteria groups must be satisfied in this auth group
                    return criteriaGroups.stream().allMatch(group ->
                            isCriteriaGroupSatisfiedInAuth(group, authGroup)
                    );
                });
    }

    /**
     * Check if at least ONE criteria in the group is satisfied (OR logic)
     */
    private boolean isCriteriaGroupSatisfiedInAuth(List<Criteria> criteriaGroup, List<agr_1251> authGroup) {
        return criteriaGroup.stream().anyMatch(criteria ->
                authGroup.stream().anyMatch(perm -> matchesCriteria(perm, criteria))
        );
    }

    /**
     * Check if a permission matches a criteria
     */
    private boolean matchesCriteria(agr_1251 perm, Criteria criteria) {
        // Check object matches
        if (!criteria.getObject().equals(perm.getObject())) {
            return false;
        }

        // Check field matches (if field is specified)
        if (criteria.getField() != null && !criteria.getField().isEmpty()) {
            if (!criteria.getField().equals(perm.getField())) {
                return false;
            }
        }

        // Check value matches
        return matchesValue(perm, criteria.getValue());
    }


    private boolean matchesValue(agr_1251 perm, String criteriaValue) {
        String permLow = perm.getLow();
        String permHigh = perm.getHigh();

        if ("*".equals(permLow)) {
            return true;
        }

        if ("*".equals(criteriaValue)) {
            return "*".equals(permLow);
        }


        boolean isRange = permHigh != null && !permHigh.isEmpty() && !permHigh.equals(permLow);

        if (isRange) {
            return criteriaValue.compareTo(permLow) >= 0 && criteriaValue.compareTo(permHigh) <= 0;
        } else {
            return permLow.equals(criteriaValue);
        }
    }


    private Set<String> extractProfiles(List<agr_1251> permissions) {
        return permissions.stream()
                .map(agr_1251::getAuth)
                .filter(auth -> auth != null && auth.length() > 2)
                .map(auth -> auth.substring(0, auth.length() - 2))
                .collect(Collectors.toSet());
    }


    private List<UserLevelOutput> findUsersWithProfiles(
            RiskRequest request,
            Map<String, Set<String>> profileToRolesMap) {

        if (profileToRolesMap.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> allProfiles = profileToRolesMap.keySet();
        List<ust04> users = ustRepository.findByProfileIn(allProfiles);

        return users.stream()
                .flatMap(user -> {
                    Set<String> roles = profileToRolesMap.getOrDefault(user.getProfile(), Collections.emptySet());
                    return roles.stream().map(role ->
                            new UserLevelOutput(
                                    request.getRiskId(),
                                    request.getDescription(),
                                    user.getBName(),
                                    role,
                                    user.getProfile()
                            )
                    );
                })
                .collect(Collectors.toList());
    }
}