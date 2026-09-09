package com.jobassist.repository;

import com.jobassist.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Project entity operations
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find projects by category
     */
    List<Project> findByCategory(String category);

    /**
     * Find projects that contain any of the specified skills
     * Uses PostgreSQL jsonb_exists_any function
     */
    @Query(value = "SELECT * FROM projects WHERE jsonb_exists_any(skills, CAST(:skills AS text[]))", nativeQuery = true)
    List<Project> findBySkillsIn(@Param("skills") String[] skills);

    /**
     * Find all projects ordered by title
     */
    List<Project> findAllByOrderByTitleAsc();
}
