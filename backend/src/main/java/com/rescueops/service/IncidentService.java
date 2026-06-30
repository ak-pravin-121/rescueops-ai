package com.rescueops.service;

import com.rescueops.dto.IncidentDtos.AddFileRequest;
import com.rescueops.dto.IncidentDtos.CreateIncidentRequest;
import com.rescueops.dto.IncidentDtos.IncidentFileResponse;
import com.rescueops.dto.IncidentDtos.IncidentResponse;
import com.rescueops.dto.IncidentDtos.UpdateIncidentRequest;
import com.rescueops.entity.Incident;
import com.rescueops.entity.IncidentFile;
import com.rescueops.entity.IncidentStatus;
import com.rescueops.entity.MessageSenderType;
import com.rescueops.entity.User;
import com.rescueops.exception.ResourceNotFoundException;
import com.rescueops.repository.IncidentFileRepository;
import com.rescueops.repository.IncidentRepository;
import com.rescueops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentFileRepository fileRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    @Transactional
    public IncidentResponse createIncident(CreateIncidentRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .serviceName(request.getServiceName())
                .severity(request.getSeverity() != null ? request.getSeverity() : com.rescueops.entity.Severity.MEDIUM)
                .status(IncidentStatus.OPEN)
                .createdBy(creator)
                .build();

        incident = incidentRepository.save(incident);

        messageService.postSystemMessage(incident.getId(),
                "Incident opened by " + creator.getName() + ": " + incident.getTitle(),
                MessageSenderType.SYSTEM);

        return toResponse(incident);
    }

    public List<IncidentResponse> listIncidents(com.rescueops.entity.Severity severity, IncidentStatus status, String serviceName) {
        return incidentRepository.findWithFilters(severity, status, serviceName).stream()
                .map(this::toResponse)
                .toList();
    }

    public IncidentResponse getIncident(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public IncidentResponse updateIncident(Long id, UpdateIncidentRequest request, Long actingUserId) {
        Incident incident = findOrThrow(id);
        IncidentStatus previousStatus = incident.getStatus();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            incident.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            incident.setDescription(request.getDescription());
        }
        if (request.getSeverity() != null) {
            incident.setSeverity(request.getSeverity());
        }
        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            incident.setAssignedTo(assignee);
        }
        if (request.getStatus() != null) {
            incident.setStatus(request.getStatus());
            if (request.getStatus() == IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
                incident.setResolvedAt(Instant.now());
            }
        }

        incident = incidentRepository.save(incident);

        if (request.getStatus() != null && request.getStatus() != previousStatus) {
            User actingUser = userRepository.findById(actingUserId).orElse(null);
            String actorName = actingUser != null ? actingUser.getName() : "Someone";
            messageService.postSystemMessage(incident.getId(),
                    actorName + " changed status from " + previousStatus + " to " + incident.getStatus(),
                    MessageSenderType.SYSTEM);
        }

        return toResponse(incident);
    }

    @Transactional
    public IncidentFileResponse addFile(Long incidentId, AddFileRequest request) {
        Incident incident = findOrThrow(incidentId);

        IncidentFile file = IncidentFile.builder()
                .incident(incident)
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .content(request.getContent())
                .build();

        file = fileRepository.save(file);
        return toFileResponse(file);
    }

    public List<IncidentFileResponse> listFiles(Long incidentId) {
        findOrThrow(incidentId);
        return fileRepository.findByIncident_IdOrderByCreatedAtAsc(incidentId).stream()
                .map(this::toFileResponse)
                .toList();
    }

    /** Concatenated file contents for an incident, used as evidence fed to the AI agents. */
    public String combinedLogContent(Long incidentId) {
        List<IncidentFile> files = fileRepository.findByIncident_IdOrderByCreatedAtAsc(incidentId);
        if (files.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (IncidentFile file : files) {
            sb.append("--- ").append(file.getFileName()).append(" (").append(file.getFileType()).append(") ---\n");
            sb.append(file.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    public List<String> listServiceNames() {
        return incidentRepository.findDistinctServiceNames();
    }

    /** Applies an AI agent's finding directly onto the incident record (severity, root cause, suggested fix). */
    @Transactional
    public void applyAgentFinding(Long incidentId, com.rescueops.entity.Severity severity, String rootCause, String suggestedFix) {
        Incident incident = findOrThrow(incidentId);
        if (severity != null) {
            incident.setSeverity(severity);
        }
        if (rootCause != null) {
            incident.setRootCause(rootCause);
        }
        if (suggestedFix != null) {
            incident.setSuggestedFix(suggestedFix);
        }
        incidentRepository.save(incident);
    }

    public Incident findOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
    }

    private IncidentResponse toResponse(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .serviceName(incident.getServiceName())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .rootCause(incident.getRootCause())
                .suggestedFix(incident.getSuggestedFix())
                .createdById(incident.getCreatedBy().getId())
                .createdByName(incident.getCreatedBy().getName())
                .assignedToId(incident.getAssignedTo() != null ? incident.getAssignedTo().getId() : null)
                .assignedToName(incident.getAssignedTo() != null ? incident.getAssignedTo().getName() : null)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolvedAt(incident.getResolvedAt())
                .build();
    }

    private IncidentFileResponse toFileResponse(IncidentFile file) {
        return IncidentFileResponse.builder()
                .id(file.getId())
                .incidentId(file.getIncident().getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .content(file.getContent())
                .createdAt(file.getCreatedAt())
                .build();
    }
}
