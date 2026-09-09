package com.jobassist.repository;

import com.jobassist.model.EnhancementSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for EnhancementSession entity operations
 */
@Repository
public interface EnhancementSessionRepository extends JpaRepository<EnhancementSession, Long> {

    /**
     * Find all enhancement sessions for a specific resume
     */
    List<EnhancementSession> findByResumeId(Long resumeId);

    /**
     * Find all enhancement sessions ordered by creation date (most recent first)
     */
    List<EnhancementSession> findAllByOrderByCreatedAtDesc();
}
