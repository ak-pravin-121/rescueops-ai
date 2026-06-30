package com.rescueops.repository;

import com.rescueops.entity.IncidentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentFileRepository extends JpaRepository<IncidentFile, Long> {

    List<IncidentFile> findByIncident_IdOrderByCreatedAtAsc(Long incidentId);
}
