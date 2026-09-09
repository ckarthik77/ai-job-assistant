package com.jobassist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a resume enhancement session
 * Tracks the before/after ATS scores and modifications made
 */
@Entity
@Table(name = "enhancement_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhancementSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    @Column(name = "original_ats_score", nullable = false)
    private Integer originalAtsScore;

    @Column(name = "enhanced_ats_score", nullable = false)
    private Integer enhancedAtsScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "added_keywords", columnDefinition = "jsonb")
    private List<String> addedKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "added_projects", columnDefinition = "jsonb")
    private List<Long> addedProjects;

    @Column(name = "enhanced_resume_url", length = 500)
    private String enhancedResumeUrl;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
