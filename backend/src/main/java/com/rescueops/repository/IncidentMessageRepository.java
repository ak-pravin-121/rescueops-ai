package com.rescueops.repository;

import com.rescueops.entity.IncidentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentMessageRepository extends JpaRepository<IncidentMessage, Long> {

    List<IncidentMessage> findByIncident_IdOrderByCreatedAtAsc(Long incidentId);
}
