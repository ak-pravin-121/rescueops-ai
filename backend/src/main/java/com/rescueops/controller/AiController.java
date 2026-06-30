package com.rescueops.controller;

import com.rescueops.ai.AiOrchestratorService;
import com.rescueops.dto.AiDtos.AgentRequest;
import com.rescueops.dto.AiDtos.AiAnalysisResponse;
import com.rescueops.dto.AiDtos.FullTriageResponse;
import com.rescueops.dto.AiDtos.RunbookResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiOrchestratorService aiOrchestratorService;

    @PostMapping("/analyze")
    public ResponseEntity<AiAnalysisResponse> analyze(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.runAnalyzer(request.getIncidentId(), request.getExtraContext()));
    }

    @PostMapping("/root-cause")
    public ResponseEntity<AiAnalysisResponse> rootCause(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.runRootCause(request.getIncidentId(), request.getExtraContext()));
    }

    @PostMapping("/runbook")
    public ResponseEntity<RunbookResponse> runbook(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.runRunbook(request.getIncidentId()));
    }

    @PostMapping("/postmortem")
    public ResponseEntity<AiAnalysisResponse> postmortem(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.runPostmortem(request.getIncidentId()));
    }

    @PostMapping("/security-scan")
    public ResponseEntity<AiAnalysisResponse> securityScan(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.runSecurityScan(request.getIncidentId(), request.getExtraContext()));
    }

    @PostMapping("/full-triage")
    public ResponseEntity<FullTriageResponse> fullTriage(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.runFullTriage(request.getIncidentId(), request.getExtraContext()));
    }

    @GetMapping("/history/{incidentId}")
    public ResponseEntity<List<AiAnalysisResponse>> history(@PathVariable Long incidentId) {
        return ResponseEntity.ok(aiOrchestratorService.getHistory(incidentId));
    }

    @GetMapping("/security-alerts")
    public ResponseEntity<List<AiAnalysisResponse>> securityAlerts() {
        return ResponseEntity.ok(aiOrchestratorService.getRecentSecurityAlerts());
    }
}
