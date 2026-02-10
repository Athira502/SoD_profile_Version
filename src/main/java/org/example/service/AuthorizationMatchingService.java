package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.AuthorizationCondition;
import org.example.model.ust12;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthorizationMatchingService {

    /**
     * Check if authorization records match the given conditions
     */
    public boolean matchesConditions(
            List<ust12> authRecords,
            List<AuthorizationCondition> conditions) {

        // Group records by field
        Map<String, List<ust12>> recordsByField = authRecords.stream()
                .collect(Collectors.groupingBy(ust12::getAuthField));

        // Check each condition
        for (AuthorizationCondition condition : conditions) {
            List<ust12> fieldRecords = recordsByField.get(condition.getField());

            if (fieldRecords == null || fieldRecords.isEmpty()) {
                // If condition is AND and field is missing, fail
                if ("AND".equalsIgnoreCase(condition.getCondition())) {
                    return false;
                }
                continue;
            }

            boolean fieldMatches = fieldRecords.stream()
                    .anyMatch(record -> matchesValue(record, condition));

            if (!fieldMatches && "AND".equalsIgnoreCase(condition.getCondition())) {
                return false;
            }
        }

        // Check for AND conditions across different fields
        return validateAndConditions(authRecords, conditions);
    }

    /**
     * Validate AND conditions across all fields
     */
    private boolean validateAndConditions(
            List<ust12> authRecords,
            List<AuthorizationCondition> conditions) {

        List<AuthorizationCondition> andConditions = conditions.stream()
                .filter(c -> "AND".equalsIgnoreCase(c.getCondition()))
                .collect(Collectors.toList());

        if (andConditions.isEmpty()) {
            return true;
        }

        // All AND conditions must be satisfied
        for (AuthorizationCondition condition : andConditions) {
            boolean found = authRecords.stream()
                    .filter(r -> r.getAuthField().equals(condition.getField()))
                    .anyMatch(r -> matchesValue(r, condition));

            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a record matches a condition value
     */
    private boolean matchesValue(ust12 record, AuthorizationCondition condition) {
        String low = record.getLow();
        String high = record.getHigh();
        String valFrom = condition.getValFrom();
        String valTo = condition.getValTo();

        // Handle wildcard
        if ("*".equals(low) || "*".equals(high)) {
            return true;
        }

        // Handle exact match
        if (valTo == null || valTo.isEmpty()) {
            return matchesSingleValue(low, high, valFrom);
        }

        // Handle range
        return matchesRange(low, high, valFrom, valTo);
    }

    /**
     * Match single value
     */
    private boolean matchesSingleValue(String low, String high, String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        // Exact match
        if (value.equals(low)) {
            return true;
        }

        // Range match (if high is specified)
        if (high != null && !high.isEmpty() && !high.equals(low)) {
            return isInRange(value, low, high);
        }

        return false;
    }

    /**
     * Match range of values
     */
    private boolean matchesRange(String low, String high, String valFrom, String valTo) {
        // Check if record range overlaps with condition range
        return rangesOverlap(low, high, valFrom, valTo);
    }

    /**
     * Check if value is in range
     */
    private boolean isInRange(String value, String rangeStart, String rangeEnd) {
        try {
            // Try numeric comparison
            double val = Double.parseDouble(value);
            double start = Double.parseDouble(rangeStart);
            double end = Double.parseDouble(rangeEnd);
            return val >= start && val <= end;
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            return value.compareTo(rangeStart) >= 0 && value.compareTo(rangeEnd) <= 0;
        }
    }

    /**
     * Check if two ranges overlap
     */
    private boolean rangesOverlap(String low1, String high1, String low2, String high2) {
        if (high1 == null || high1.isEmpty()) {
            high1 = low1;
        }
        if (high2 == null || high2.isEmpty()) {
            high2 = low2;
        }

        try {
            // Try numeric comparison
            double start1 = Double.parseDouble(low1);
            double end1 = Double.parseDouble(high1);
            double start2 = Double.parseDouble(low2);
            double end2 = Double.parseDouble(high2);

            return start1 <= end2 && start2 <= end1;
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            return low1.compareTo(high2) <= 0 && low2.compareTo(high1) <= 0;
        }
    }
}