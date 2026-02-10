package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.*;
import org.example.service.RiskAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/risk-analysis")
@RequiredArgsConstructor
@Slf4j
public class RiskAnalysisController {

    private final RiskAnalysisService riskAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeRisk(
            @Valid @RequestBody RiskAnalysisRequest request) {

        log.info("Received risk analysis request for risk: {}",
                request.getRiskDefinition().getRiskId());

        Map<String, Object> result = riskAnalysisService.analyzeRisk(request);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Risk Analysis Service is running");
    }
}