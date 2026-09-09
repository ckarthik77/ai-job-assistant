import React, { useState } from 'react';
import { FileText, Briefcase, Building } from 'lucide-react';

/**
 * Job description input component with text area and optional file upload
 */
const JobDescriptionInput = ({ onJobSubmit, jobData, setJobData }) => {
  const [errors, setErrors] = useState({});

  const handleInputChange = (field, value) => {
    setJobData(prev => ({ ...prev, [field]: value }));
    // Clear error for this field
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: null }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!jobData.descriptionText || jobData.descriptionText.trim().length < 50) {
      newErrors.descriptionText = 'Job description must be at least 50 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      onJobSubmit();
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label className="form-label">
          <Building size={16} style={{ display: 'inline', marginRight: '0.5rem' }} />
          Company Name (Optional)
        </label>
        <input
          type="text"
          className="input"
          placeholder="e.g., Google, Amazon, Microsoft"
          value={jobData.companyName}
          onChange={(e) => handleInputChange('companyName', e.target.value)}
        />
      </div>

      <div className="form-group">
        <label className="form-label">
          <Briefcase size={16} style={{ display: 'inline', marginRight: '0.5rem' }} />
          Job Title (Optional)
        </label>
        <input
          type="text"
          className="input"
          placeholder="e.g., Senior Software Engineer, Full Stack Developer"
          value={jobData.jobTitle}
          onChange={(e) => handleInputChange('jobTitle', e.target.value)}
        />
      </div>

      <div className="form-group">
        <label className="form-label">
          <FileText size={16} style={{ display: 'inline', marginRight: '0.5rem' }} />
          Job Description *
        </label>
        <textarea
          className={`textarea ${errors.descriptionText ? 'error' : ''}`}
          placeholder="Paste the full job description here... Include requirements, qualifications, and responsibilities."
          value={jobData.descriptionText}
          onChange={(e) => handleInputChange('descriptionText', e.target.value)}
          rows={12}
        />
        {errors.descriptionText && (
          <div className="alert alert-error" style={{ marginTop: '0.5rem' }}>
            {errors.descriptionText}
          </div>
        )}
        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
          {jobData.descriptionText.length} characters
        </p>
      </div>
    </form>
  );
};

export default JobDescriptionInput;
