package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AnalysisResult {
    private List<RoleLevelOutput> roleResults;
    private List<UserLevelOutput> userResults;
}