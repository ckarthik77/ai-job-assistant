-- AI Job Application Assistant Database Schema
-- PostgreSQL 17

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS enhancement_sessions CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS job_descriptions CASCADE;
DROP TABLE IF EXISTS resumes CASCADE;

-- Resumes table: stores uploaded resume data
CREATE TABLE resumes (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255),
    original_content TEXT NOT NULL,
    skills JSONB DEFAULT '[]'::jsonb,
    experience JSONB DEFAULT '[]'::jsonb,
    education JSONB DEFAULT '[]'::jsonb,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Job Descriptions table: stores job postings and extracted keywords
CREATE TABLE job_descriptions (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255),
    job_title VARCHAR(255),
    description_text TEXT NOT NULL,
    required_skills JSONB DEFAULT '[]'::jsonb,
    keywords JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Projects table: pre-seeded project library for resume enhancement
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    skills JSONB DEFAULT '[]'::jsonb,
    category VARCHAR(50) NOT NULL CHECK (category IN ('backend', 'frontend', 'fullstack', 'ml', 'devops', 'mobile', 'data'))
);

-- Enhancement Sessions table: tracks resume enhancement history
CREATE TABLE enhancement_sessions (
    id BIGSERIAL PRIMARY KEY,
    resume_id BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    job_description_id BIGINT NOT NULL REFERENCES job_descriptions(id) ON DELETE CASCADE,
    original_ats_score INTEGER NOT NULL CHECK (original_ats_score >= 0 AND original_ats_score <= 100),
    enhanced_ats_score INTEGER NOT NULL CHECK (enhanced_ats_score >= 0 AND enhanced_ats_score <= 100),
    added_keywords JSONB DEFAULT '[]'::jsonb,
    added_projects JSONB DEFAULT '[]'::jsonb,
    enhanced_resume_url VARCHAR(500),
    processing_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_resumes_email ON resumes(user_email);
CREATE INDEX idx_resumes_uploaded_at ON resumes(uploaded_at DESC);
CREATE INDEX idx_job_descriptions_created_at ON job_descriptions(created_at DESC);
CREATE INDEX idx_projects_category ON projects(category);
CREATE INDEX idx_enhancement_sessions_resume_id ON enhancement_sessions(resume_id);
CREATE INDEX idx_enhancement_sessions_created_at ON enhancement_sessions(created_at DESC);

-- Create GIN indexes for JSONB columns for efficient searching
CREATE INDEX idx_resumes_skills ON resumes USING GIN (skills);
CREATE INDEX idx_job_descriptions_keywords ON job_descriptions USING GIN (keywords);
CREATE INDEX idx_projects_skills ON projects USING GIN (skills);
