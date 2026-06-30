package com.rescueops.repository;

import com.rescueops.entity.AiAnalysis;
import com.rescueops.entity.AnalysisType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {

    List<AiAnalysis> findByIncident_IdOrderByCreatedAtAsc(Long incidentId);

    List<AiAnalysis> findByAnalysisTypeOrderByCreatedAtDesc(AnalysisType analysisType);
}
