import React from 'react';
import { Download, FileText, Tag, Folder } from 'lucide-react';
import { getDownloadUrl } from '../services/api';

/**
 * Enhanced resume viewer component showing changes and download option
 */
const EnhancedResumeViewer = ({ sessionId, addedKeywords, addedProjects }) => {
  const handleDownload = () => {
    window.open(getDownloadUrl(sessionId), '_blank');
  };

  return (
    <div>
      <h3 className="card-title">
        <FileText size={24} style={{ display: 'inline', marginRight: '0.5rem', verticalAlign: 'middle' }} />
        Enhanced Resume Summary
      </h3>

      {/* Added Keywords */}
      {addedKeywords && addedKeywords.length > 0 && (
        <div style={{ marginBottom: '2rem' }}>
          <h4 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Tag size={18} />
            Added Keywords ({addedKeywords.length})
          </h4>
          <div className="keywords-list">
            {addedKeywords.map((keyword, index) => (
              <span key={index} className="keyword-tag">
                {keyword}
              </span>
            ))}
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: '1rem' }}>
            These keywords have been naturally integrated into your resume to improve ATS matching.
          </p>
        </div>
      )}

      {/* Added Projects */}
      {addedProjects && addedProjects.length > 0 && (
        <div style={{ marginBottom: '2rem' }}>
          <h4 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Folder size={18} />
            Added Projects ({addedProjects.length})
          </h4>
          <div className="projects-list">
            {addedProjects.map((project) => (
              <div key={project.id} className="project-item">
                <div className="project-title">{project.title}</div>
                <span className="project-category">{project.category}</span>
              </div>
            ))}
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: '1rem' }}>
            These projects have been added in Google XYZ format to showcase relevant experience.
          </p>
        </div>
      )}

      {/* Download Section */}
      <div style={{
        textAlign: 'center',
        padding: '2rem',
        background: 'var(--bg-secondary)',
        borderRadius: 'var(--radius)',
        marginTop: '2rem'
      }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', marginBottom: '1rem' }}>
          Your Enhanced Resume is Ready!
        </h4>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
          Download your professionally formatted, ATS-optimized resume in PDF format.
        </p>
        <button onClick={handleDownload} className="btn btn-success" style={{ fontSize: '1.125rem' }}>
          <Download size={20} />
          Download Enhanced Resume
        </button>
      </div>

      {/* Tips */}
      <div style={{
        marginTop: '2rem',
        padding: '1.5rem',
        background: 'rgba(79, 70, 229, 0.05)',
        borderRadius: 'var(--radius)',
        borderLeft: '4px solid var(--primary-color)'
      }}>
        <h5 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '0.75rem' }}>
          💡 Next Steps
        </h5>
        <ul style={{ paddingLeft: '1.5rem', color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: '1.8' }}>
          <li>Review the enhanced resume and customize as needed</li>
          <li>Ensure all projects and keywords accurately reflect your experience</li>
          <li>Proofread for any errors or inconsistencies</li>
          <li>Tailor the content further for specific job applications</li>
          <li>Save multiple versions for different types of positions</li>
        </ul>
      </div>
    </div>
  );
};

export default EnhancedResumeViewer;
