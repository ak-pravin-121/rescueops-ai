package com.rescueops.repository;

import com.rescueops.entity.Incident;
import com.rescueops.entity.IncidentStatus;
import com.rescueops.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @Query("""
            SELECT i FROM Incident i
            WHERE (:severity IS NULL OR i.severity = :severity)
            AND (:status IS NULL OR i.status = :status)
            AND (:serviceName IS NULL OR LOWER(i.serviceName) = LOWER(:serviceName))
            ORDER BY i.createdAt DESC
            """)
    List<Incident> findWithFilters(@Param("severity") Severity severity,
                                    @Param("status") IncidentStatus status,
                                    @Param("serviceName") String serviceName);

    List<Incident> findByServiceNameIgnoreCaseAndCreatedAtAfter(String serviceName, Instant after);

    List<Incident> findByCreatedAtAfter(Instant after);

    long countByStatus(IncidentStatus status);

    long countBySeverity(Severity severity);

    @Query("SELECT DISTINCT i.serviceName FROM Incident i")
    List<String> findDistinctServiceNames();
}
