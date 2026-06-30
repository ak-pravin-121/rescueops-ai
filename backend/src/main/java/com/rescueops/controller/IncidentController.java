package com.rescueops.controller;

import com.rescueops.dto.IncidentDtos.AddFileRequest;
import com.rescueops.dto.IncidentDtos.CreateIncidentRequest;
import com.rescueops.dto.IncidentDtos.IncidentFileResponse;
import com.rescueops.dto.IncidentDtos.IncidentResponse;
import com.rescueops.dto.IncidentDtos.UpdateIncidentRequest;
import com.rescueops.dto.MessageDtos.SendMessageRequest;
import com.rescueops.dto.MessageDtos.MessageResponse;
import com.rescueops.entity.IncidentStatus;
import com.rescueops.entity.Severity;
import com.rescueops.security.UserPrincipal;
import com.rescueops.service.IncidentService;
import com.rescueops.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody CreateIncidentRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.createIncident(request, principal.getId()));
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> list(@RequestParam(required = false) Severity severity,
                                                        @RequestParam(required = false) IncidentStatus status,
                                                        @RequestParam(required = false) String serviceName) {
        return ResponseEntity.ok(incidentService.listIncidents(severity, status, serviceName));
    }

    @GetMapping("/meta/services")
    public ResponseEntity<List<String>> listServiceNames() {
        return ResponseEntity.ok(incidentService.listServiceNames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncident(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(@PathVariable Long id,
                                                    @RequestBody UpdateIncidentRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request, principal.getId()));
    }

    @PostMapping("/{id}/files")
    public ResponseEntity<IncidentFileResponse> addFile(@PathVariable Long id,
                                                         @Valid @RequestBody AddFileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.addFile(id, request));
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<List<IncidentFileResponse>> listFiles(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.listFiles(id));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getHistory(id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> postMessage(@PathVariable Long id,
                                                        @Valid @RequestBody SendMessageRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.postUserMessage(id, request.getMessage(), principal.getId()));
    }
}
