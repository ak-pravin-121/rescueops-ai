package com.rescueops.service;

import com.rescueops.dto.MessageDtos.MessageResponse;
import com.rescueops.entity.Incident;
import com.rescueops.entity.IncidentMessage;
import com.rescueops.entity.MessageSenderType;
import com.rescueops.entity.User;
import com.rescueops.exception.ResourceNotFoundException;
import com.rescueops.repository.IncidentMessageRepository;
import com.rescueops.repository.IncidentRepository;
import com.rescueops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final IncidentMessageRepository messageRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageResponse postUserMessage(Long incidentId, String text, Long senderId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        IncidentMessage message = IncidentMessage.builder()
                .incident(incident)
                .sender(sender)
                .senderType(MessageSenderType.USER)
                .message(text)
                .build();

        return broadcastAndReturn(message);
    }

    /** Used by AI agents / status changes to post system-authored updates into the live war room. */
    @Transactional
    public MessageResponse postSystemMessage(Long incidentId, String text, MessageSenderType senderType) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        IncidentMessage message = IncidentMessage.builder()
                .incident(incident)
                .sender(null)
                .senderType(senderType)
                .message(text)
                .build();

        return broadcastAndReturn(message);
    }

    public List<MessageResponse> getHistory(Long incidentId) {
        return messageRepository.findByIncident_IdOrderByCreatedAtAsc(incidentId).stream()
                .map(this::toResponse)
                .toList();
    }

    private MessageResponse broadcastAndReturn(IncidentMessage message) {
        IncidentMessage saved = messageRepository.save(message);
        MessageResponse response = toResponse(saved);
        messagingTemplate.convertAndSend("/topic/incidents/" + saved.getIncident().getId(), response);
        return response;
    }

    private MessageResponse toResponse(IncidentMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .incidentId(message.getIncident().getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? message.getSender().getName() : message.getSenderType().name())
                .senderType(message.getSenderType())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
