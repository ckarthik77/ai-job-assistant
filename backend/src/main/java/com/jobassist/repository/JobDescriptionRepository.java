package com.jobassist.repository;

import com.jobassist.model.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for JobDescription entity operations
 */
@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {

    /**
     * Find job descriptions by company name
     */
    List<JobDescription> findByCompanyName(String companyName);

    /**
     * Find job descriptions ordered by creation date (most recent first)
     */
    List<JobDescription> findAllByOrderByCreatedAtDesc();
}
