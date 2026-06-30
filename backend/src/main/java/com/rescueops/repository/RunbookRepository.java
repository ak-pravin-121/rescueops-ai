package com.rescueops.repository;

import com.rescueops.entity.Runbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RunbookRepository extends JpaRepository<Runbook, Long> {

    List<Runbook> findByIncident_IdOrderByCreatedAtDesc(Long incidentId);
}
