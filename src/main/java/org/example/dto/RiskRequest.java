package org.example.dto;
import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RiskRequest {
    private String riskId;
    private String description;
    private String logic;
    private List<FunctionRule> functions;
}


