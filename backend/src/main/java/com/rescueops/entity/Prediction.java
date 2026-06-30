package com.rescueops.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String serviceName;

    /** 0-100 risk score for this service failing again soon. */
    @Column(nullable = false)
    private Double riskScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String predictedFailure;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reasoning;

    /** Statistical inputs that fed the model, kept for transparency/audit. */
    private Integer incidentCountLast30Days;

    private Double criticalRatioLast30Days;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
