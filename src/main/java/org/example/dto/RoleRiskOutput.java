package org.example.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRiskOutput {
    private String riskId;
    private String description;
    private String roleId;
    private String riskLevel;
    private String riskType;
}