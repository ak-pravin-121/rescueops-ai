package com.rescueops.ai;

import com.rescueops.dto.AiDtos.AiAnalysisResponse;
import com.rescueops.dto.AiDtos.FullTriageResponse;
import com.rescueops.dto.AiDtos.RunbookResponse;
import com.rescueops.entity.AiAnalysis;
import com.rescueops.entity.AnalysisType;
import com.rescueops.entity.Incident;
import com.rescueops.entity.MessageSenderType;
import com.rescueops.entity.Runbook;
import com.rescueops.rag.KnowledgeService;
import com.rescueops.repository.AiAnalysisRepository;
import com.rescueops.repository.RunbookRepository;
import com.rescueops.service.IncidentService;
import com.rescueops.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiOrchestratorService {

    private final IncidentAnalyzerAgent analyzerAgent;
    private final RootCauseAgent rootCauseAgent;
    private final RunbookAgent runbookAgent;
    private final PostmortemAgent postmortemAgent;
    private final SecurityAgent securityAgent;

    private final IncidentService incidentService;
    private final KnowledgeService knowledgeService;
    private final MessageService messageService;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final RunbookRepository runbookRepository;

    @Transactional
    public AiAnalysisResponse runAnalyzer(Long incidentId, String extraContext) {
        Incident incident = incidentService.findOrThrow(incidentId);
        String logs = combinedContext(incidentId, extraContext);

        AgentFinding finding = analyzerAgent.analyze(incident.getTitle(), incident.getDescription(), logs);

        AiAnalysis saved = persistAnalysis(incident, AnalysisType.ANALYSIS, finding);
        incidentService.applyAgentFinding(incidentId, finding.getSeverity(), finding.getRootCause(), finding.getSuggestedFix());

        messageService.postSystemMessage(incidentId,
                "Analyzer Agent: severity=" + finding.getSeverity() + ", confidence=" + finding.getConfidence()
                        + "%. " + finding.getRootCause(),
                MessageSenderType.AI);

        return toResponse(saved);
    }

    @Transactional
    public AiAnalysisResponse runRootCause(Long incidentId, String extraContext) {
        Incident incident = incidentService.findOrThrow(incidentId);
        String logs = combinedContext(incidentId, extraContext);

        AgentFinding prior = AgentFinding.builder()
                .severity(incident.getSeverity())
                .rootCause(incident.getRootCause())
                .suggestedFix(incident.getSuggestedFix())
                .confidence(null)
                .build();

        String similarPast = knowledgeService.findSimilarPastIncidentsContext(incident.getTitle(), incident.getDescription());

        AgentFinding finding = rootCauseAgent.investigate(incident.getTitle(), incident.getDescription(), logs, prior, similarPast);

        AiAnalysis saved = persistAnalysis(incident, AnalysisType.ROOT_CAUSE, finding);
        incidentService.applyAgentFinding(incidentId, finding.getSeverity(), finding.getRootCause(), finding.getSuggestedFix());

        messageService.postSystemMessage(incidentId,
                "Root Cause Agent: " + finding.getRootCause(),
                MessageSenderType.AI);

        return toResponse(saved);
    }

    @Transactional
    public RunbookResponse runRunbook(Long incidentId) {
        Incident incident = incidentService.findOrThrow(incidentId);

        RunbookResult result = runbookAgent.generateRunbook(
                incident.getTitle(), incident.getServiceName(), incident.getRootCause(), incident.getSuggestedFix());

        Runbook runbook = Runbook.builder()
                .incident(incident)
                .title(result.getTitle())
                .content(result.getContent())
                .build();
        runbook = runbookRepository.save(runbook);

        aiAnalysisRepository.save(AiAnalysis.builder()
                .incident(incident)
                .analysisType(AnalysisType.RUNBOOK)
                .content(result.getContent())
                .build());

        messageService.postSystemMessage(incidentId,
                "Runbook Agent generated a remediation runbook with " + countSteps(result.getContent()) + " steps.",
                MessageSenderType.AI);

        return RunbookResponse.builder()
                .id(runbook.getId())
                .incidentId(incidentId)
                .title(runbook.getTitle())
                .content(runbook.getContent())
                .createdAt(runbook.getCreatedAt())
                .build();
    }

    /** The literal Analyzer -> Root Cause -> Runbook chain, each step grounded in the previous one's output. */
    @Transactional
    public FullTriageResponse runFullTriage(Long incidentId, String extraContext) {
        messageService.postSystemMessage(incidentId, "Starting multi-agent triage pipeline...", MessageSenderType.SYSTEM);

        AiAnalysisResponse analysis = runAnalyzer(incidentId, extraContext);
        AiAnalysisResponse rootCause = runRootCause(incidentId, extraContext);
        RunbookResponse runbook = runRunbook(incidentId);

        messageService.postSystemMessage(incidentId, "Multi-agent triage pipeline complete.", MessageSenderType.SYSTEM);

        return FullTriageResponse.builder()
                .analysis(analysis)
                .rootCause(rootCause)
                .runbook(runbook)
                .build();
    }

    @Transactional
    public AiAnalysisResponse runPostmortem(Long incidentId) {
        Incident incident = incidentService.findOrThrow(incidentId);
        List<Runbook> runbooks = runbookRepository.findByIncident_IdOrderByCreatedAtDesc(incidentId);
        String runbookContent = runbooks.isEmpty() ? null : runbooks.get(0).getContent();

        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        String postmortem = postmortemAgent.generatePostmortem(
                incident.getTitle(), incident.getDescription(), incident.getServiceName(),
                incident.getSeverity().name(), incident.getRootCause(), incident.getSuggestedFix(),
                runbookContent,
                fmt.format(incident.getCreatedAt()),
                incident.getResolvedAt() != null ? fmt.format(incident.getResolvedAt()) : null);

        AiAnalysis saved = aiAnalysisRepository.save(AiAnalysis.builder()
                .incident(incident)
                .analysisType(AnalysisType.POSTMORTEM)
                .content(postmortem)
                .build());

        messageService.postSystemMessage(incidentId, "Postmortem Agent generated a blameless postmortem.", MessageSenderType.AI);

        return toResponse(saved);
    }

    @Transactional
    public AiAnalysisResponse runSecurityScan(Long incidentId, String extraContext) {
        Incident incident = incidentService.findOrThrow(incidentId);
        String logs = combinedContext(incidentId, extraContext);

        AgentFinding finding = securityAgent.scan(incident.getTitle(), logs);
        AiAnalysis saved = persistAnalysis(incident, AnalysisType.SECURITY, finding);

        messageService.postSystemMessage(incidentId,
                "Security Agent: " + finding.getRootCause() + " (threat severity: " + finding.getSeverity() + ")",
                MessageSenderType.AI);

        return toResponse(saved);
    }

    public List<AiAnalysisResponse> getHistory(Long incidentId) {
        return aiAnalysisRepository.findByIncident_IdOrderByCreatedAtAsc(incidentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AiAnalysisResponse> getRecentSecurityAlerts() {
        return aiAnalysisRepository.findByAnalysisTypeOrderByCreatedAtDesc(AnalysisType.SECURITY).stream()
                .map(this::toResponse)
                .toList();
    }

    private String combinedContext(Long incidentId, String extraContext) {
        String stored = incidentService.combinedLogContent(incidentId);
        if (extraContext == null || extraContext.isBlank()) {
            return stored;
        }
        return stored + "\n--- additional context ---\n" + extraContext;
    }

    private AiAnalysis persistAnalysis(Incident incident, AnalysisType type, AgentFinding finding) {
        AiAnalysis analysis = AiAnalysis.builder()
                .incident(incident)
                .analysisType(type)
                .severity(finding.getSeverity())
                .rootCause(finding.getRootCause())
                .suggestedFix(finding.getSuggestedFix())
                .confidence(finding.getConfidence())
                .content(finding.getRootCause())
                .build();
        return aiAnalysisRepository.save(analysis);
    }

    private int countSteps(String runbookContent) {
        if (runbookContent == null) {
            return 0;
        }
        return (int) runbookContent.lines().filter(l -> l.trim().matches("^\\d+[.)].*")).count();
    }

    private AiAnalysisResponse toResponse(AiAnalysis a) {
        return AiAnalysisResponse.builder()
                .id(a.getId())
                .incidentId(a.getIncident().getId())
                .analysisType(a.getAnalysisType())
                .severity(a.getSeverity())
                .rootCause(a.getRootCause())
                .suggestedFix(a.getSuggestedFix())
                .confidence(a.getConfidence())
                .content(a.getContent())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
