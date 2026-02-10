package org.example.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskDefinition {
    private String riskId;
    private String description;
    private String riskLevel;
    private String riskType;
    private List<String> functionIds; // CP0002, CP0003, etc.
}