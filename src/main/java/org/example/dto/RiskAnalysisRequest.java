package org.example.dto;

import lombok.*;

import java.util.List;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAnalysisRequest {
    private String clientId;
    private RiskDefinition riskDefinition;
    private List<FunctionPermission> functionPermissions;
}