
package org.example.controller;

import org.example.dto.AnalysisResult;
import org.example.dto.RiskRequest;
import org.example.service.RiskAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk-analysis")
public class RiskAnalysisController {

    @Autowired
    private RiskAnalysisService riskService;

    @PostMapping("/execute")
    public ResponseEntity<AnalysisResult> executeAnalysis(
            @RequestBody RiskRequest request) {  // ✓ Correct Spring annotation
        AnalysisResult result = riskService.analyze(request);
        return ResponseEntity.ok(result);
    }
}