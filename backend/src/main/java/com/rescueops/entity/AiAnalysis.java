package com.rescueops.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ai_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisType analysisType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity severity;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rootCause;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String suggestedFix;

    private Integer confidence;

    /** Raw model output - full runbook text, postmortem doc, security findings, etc. */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
