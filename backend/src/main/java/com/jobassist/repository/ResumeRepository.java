package com.jobassist.repository;

import com.jobassist.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Resume entity operations
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Find resumes by user email
     */
    List<Resume> findByUserEmail(String userEmail);

    /**
     * Find resumes ordered by upload date (most recent first)
     */
    List<Resume> findAllByOrderByUploadedAtDesc();
}
