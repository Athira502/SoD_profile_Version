package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.*;
import org.example.model.*;
import org.example.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RiskAnalysisService {

    private final Ust12Repository ust12Repository;
    private final Ust10sRepository ust10sRepository;
    private final Ust10cRepository ust10cRepository;
    private final Ust04Repository ust04Repository;
    private final AgrProfRepository agrProfRepository;
    private final AuthorizationMatchingService authMatchingService;
    private final ProfileHierarchyService profileHierarchyService;

    public Map<String, Object> analyzeRisk(RiskAnalysisRequest request) {
        log.info("Starting risk analysis for risk: {}", request.getRiskDefinition().getRiskId());

        String clientId = request.getClientId();
        RiskDefinition riskDef = request.getRiskDefinition();
        List<FunctionPermission> functionPermissions = request.getFunctionPermissions();

        // Step 1: Get critical authorizations (usermaster_maint values) for each function
        Map<String, Set<String>> functionToUsermasterMaints = new HashMap<>();

        for (FunctionPermission funcPerm : functionPermissions) {
            Set<String> criticalUsermasterMaints = findCriticalAuthorizations(
                    clientId, funcPerm.getConditions()
            );
            functionToUsermasterMaints.put(funcPerm.getFunctionId(), criticalUsermasterMaints);
            log.info("Function {} has {} critical authorizations",
                    funcPerm.getFunctionId(), criticalUsermasterMaints.size());
        }

        // Step 2: Find users/roles that have ALL required functions (SoD violation)
        Set<String> violatingUsermasterMaints = findSoDViolations(functionToUsermasterMaints);

        if (violatingUsermasterMaints.isEmpty()) {
            log.info("No SoD violations found for risk: {}", riskDef.getRiskId());
            return createEmptyResult();
        }

        log.info("Found {} violating authorizations", violatingUsermasterMaints.size());

        // Step 3: Expand to all profiles (including composite profiles)
        Set<String> allProfiles = expandToAllProfiles(clientId, violatingUsermasterMaints);
        log.info("Expanded to {} profiles", allProfiles.size());

        // Step 4: Find users with these profiles
        List<ust04> userAssignments = ust04Repository.findByClientAndProfiles(
                clientId, new ArrayList<>(allProfiles)
        );

        // Step 5: Find roles with these profiles
        List<agr_prof> roleAssignments = agrProfRepository.findByClientAndProfiles(
                clientId, new ArrayList<>(allProfiles)
        );

        // Step 6: Generate outputs
        List<UserRiskOutput> userOutputs = generateUserOutputs(
                riskDef, userAssignments, roleAssignments
        );

        List<RoleRiskOutput> roleOutputs = generateRoleOutputs(
                riskDef, roleAssignments
        );

        Map<String, Object> result = new HashMap<>();
        result.put("userLevelRisks", userOutputs);
        result.put("roleLevelRisks", roleOutputs);
        result.put("totalUsersAffected", userOutputs.size());
        result.put("totalRolesAffected", roleOutputs.size());

        return result;
    }

    /**
     * Find critical authorizations based on complex conditions
     */
    private Set<String> findCriticalAuthorizations(
            String clientId,
            List<AuthorizationCondition> conditions) {

        // Group conditions by authorization object
        Map<String, List<AuthorizationCondition>> conditionsByObject = conditions.stream()
                .collect(Collectors.groupingBy(AuthorizationCondition::getAuthObj));

        // For each object, find matching authorizations
        Map<String, Set<String>> objectToUsermasterMaints = new HashMap<>();

        for (Map.Entry<String, List<AuthorizationCondition>> entry : conditionsByObject.entrySet()) {
            String authObj = entry.getKey();
            List<AuthorizationCondition> objConditions = entry.getValue();

            Set<String> matchingUsermasterMaints = findMatchingAuthorizationsForObject(
                    clientId, authObj, objConditions
            );
            objectToUsermasterMaints.put(authObj, matchingUsermasterMaints);
        }

        // Combine results based on AND/OR logic between different auth objects
        return combineAuthorizationResults(objectToUsermasterMaints, conditions);
    }

    /**
     * Find matching authorizations for a single authorization object
     */
    private Set<String> findMatchingAuthorizationsForObject(
            String clientId,
            String authObj,
            List<AuthorizationCondition> conditions) {

        // Get all auth data for this object
        List<ust12> allAuthData = new ArrayList<>();
        for (AuthorizationCondition condition : conditions) {
            List<ust12> data = ust12Repository.findByClientAndAuthObjAndField(
                    clientId, authObj, condition.getField()
            );
            allAuthData.addAll(data);
        }

        // Group by usermaster_maint to check if all conditions are met within same auth
        Map<String, List<ust12>> authsByUsermasterMaint = allAuthData.stream()
                .collect(Collectors.groupingBy(ust12::getUsermasterMaint));

        Set<String> result = new HashSet<>();

        for (Map.Entry<String, List<ust12>> entry : authsByUsermasterMaint.entrySet()) {
            String usermasterMaint = entry.getKey();
            List<ust12> authRecords = entry.getValue();

            // Check if this authorization satisfies the conditions
            if (authMatchingService.matchesConditions(authRecords, conditions)) {
                result.add(usermasterMaint);
            }
        }

        return result;
    }

    /**
     * Combine results from different authorization objects
     */
    private Set<String> combineAuthorizationResults(
            Map<String, Set<String>> objectToUsermasterMaints,
            List<AuthorizationCondition> conditions) {

        if (objectToUsermasterMaints.isEmpty()) {
            return new HashSet<>();
        }

        // For SoD analysis within a function, all conditions are typically AND
        // (user must have ALL specified authorizations)
        Set<String> result = null;

        for (Set<String> usermasterMaints : objectToUsermasterMaints.values()) {
            if (result == null) {
                result = new HashSet<>(usermasterMaints);
            } else {
                result.retainAll(usermasterMaints); // AND operation
            }
        }

        return result != null ? result : new HashSet<>();
    }

    /**
     * Find authorizations that violate SoD (have all required functions)
     */
    private Set<String> findSoDViolations(Map<String, Set<String>> functionToUsermasterMaints) {
        if (functionToUsermasterMaints.isEmpty()) {
            return new HashSet<>();
        }

        // Find intersection - users/auths that have ALL functions
        Set<String> violations = null;

        for (Set<String> usermasterMaints : functionToUsermasterMaints.values()) {
            if (violations == null) {
                violations = new HashSet<>(usermasterMaints);
            } else {
                violations.retainAll(usermasterMaints);
            }
        }

        return violations != null ? violations : new HashSet<>();
    }

    /**
     * Expand usermaster_maint to all profiles including composite profiles
     */
    private Set<String> expandToAllProfiles(String clientId, Set<String> usermasterMaints) {
        Set<String> allProfiles = new HashSet<>();

        // Get direct profiles from UST10S
        List<String> directProfiles = ust10sRepository.findProfilesByClientAndUsermasterMaints(
                clientId, new ArrayList<>(usermasterMaints)
        );
        allProfiles.addAll(directProfiles);

        // Add usermaster_maint values (they can also be profiles)
        allProfiles.addAll(usermasterMaints);

        // Expand composite profiles iteratively
        Set<String> expandedProfiles = profileHierarchyService.expandCompositeProfiles(
                clientId, allProfiles
        );

        return expandedProfiles;
    }

    private List<UserRiskOutput> generateUserOutputs(
            RiskDefinition riskDef,
            List<ust04> userAssignments,
            List<agr_prof> roleAssignments) {

        List<UserRiskOutput> outputs = new ArrayList<>();

        // Create a map of profile -> roles for quick lookup
        Map<String, List<String>> profileToRoles = roleAssignments.stream()
                .collect(Collectors.groupingBy(
                        agr_prof::getProfile,
                        Collectors.mapping(agr_prof::getRoleName, Collectors.toList())
                ));

        for (ust04 userAssignment : userAssignments) {
            List<String> roles = profileToRoles.getOrDefault(
                    userAssignment.getProfile(),
                    Collections.emptyList()
            );

            if (roles.isEmpty()) {
                // User has profile but no role mapping
                outputs.add(UserRiskOutput.builder()
                        .riskId(riskDef.getRiskId())
                        .description(riskDef.getDescription())
                        .userId(userAssignment.getBName())
                        .roleName("N/A")
                        .profile(userAssignment.getProfile())
                        .riskLevel(riskDef.getRiskLevel())
                        .riskType(riskDef.getRiskType())
                        .build());
            } else {
                for (String role : roles) {
                    outputs.add(UserRiskOutput.builder()
                            .riskId(riskDef.getRiskId())
                            .description(riskDef.getDescription())
                            .userId(userAssignment.getBName())
                            .roleName(role)
                            .profile(userAssignment.getProfile())
                            .riskLevel(riskDef.getRiskLevel())
                            .riskType(riskDef.getRiskType())
                            .build());
                }
            }
        }

        return outputs;
    }

    private List<RoleRiskOutput> generateRoleOutputs(
            RiskDefinition riskDef,
            List<agr_prof> roleAssignments) {

        return roleAssignments.stream()
                .map(agr -> RoleRiskOutput.builder()
                        .riskId(riskDef.getRiskId())
                        .description(riskDef.getDescription())
                        .roleId(agr.getRoleName())
                        .riskLevel(riskDef.getRiskLevel())
                        .riskType(riskDef.getRiskType())
                        .build())
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("userLevelRisks", Collections.emptyList());
        result.put("roleLevelRisks", Collections.emptyList());
        result.put("totalUsersAffected", 0);
        result.put("totalRolesAffected", 0);
        return result;
    }
}