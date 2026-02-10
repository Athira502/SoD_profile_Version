package org.example.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRiskOutput {
    private String riskId;
    private String description;
    private String userId;
    private String roleName;
    private String profile;
    private String riskLevel;
    private String riskType;
}