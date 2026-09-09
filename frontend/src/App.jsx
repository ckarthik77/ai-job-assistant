import React, { useState } from 'react';
import { CheckCircle, ArrowRight, ArrowLeft, RefreshCw } from 'lucide-react';
import ResumeUploader from './components/ResumeUploader';
import JobDescriptionInput from './components/JobDescriptionInput';
import LoadingIndicator from './components/LoadingIndicator';
import ATSScoreDisplay from './components/ATSScoreDisplay';
import EnhancedResumeViewer from './components/EnhancedResumeViewer';
import { uploadResume, uploadJobDescription, enhanceResume } from './services/api';

/**
 * Main Application Component
 * Implements step-by-step workflow for resume enhancement
 */
function App() {
  // State management
  const [currentStep, setCurrentStep] = useState(1);
  const [resumeFile, setResumeFile] = useState(null);
  const [resumeId, setResumeId] = useState(null);
  const [jobData, setJobData] = useState({
    companyName: '',
    jobTitle: '',
    descriptionText: ''
  });
  const [jobDescriptionId, setJobDescriptionId] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [enhancementResult, setEnhancementResult] = useState(null);
  const [error, setError] = useState(null);

  // Step 1: Upload Resume
  const handleResumeUpload = async () => {
    if (!resumeFile) {
      setError('Please select a resume file');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await uploadResume(resumeFile);
      setResumeId(result.resumeId);
      setCurrentStep(2);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to upload resume. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  // Step 2: Upload Job Description
  const handleJobDescriptionUpload = async () => {
    if (!jobData.descriptionText || jobData.descriptionText.trim().length < 50) {
      setError('Please enter a valid job description (at least 50 characters)');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await uploadJobDescription(jobData);
      setJobDescriptionId(result.jobDescriptionId);
      setCurrentStep(3);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to process job description. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  // Step 3: Enhance Resume
  const handleEnhanceResume = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const result = await enhanceResume(resumeId, jobDescriptionId);
      setEnhancementResult(result);
      setCurrentStep(4);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to enhance resume. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  // Reset and start over
  const handleStartOver = () => {
    setCurrentStep(1);
    setResumeFile(null);
    setResumeId(null);
    setJobData({ companyName: '', jobTitle: '', descriptionText: '' });
    setJobDescriptionId(null);
    setEnhancementResult(null);
    setError(null);
  };

  return (
    <div className="app">
      <div className="container">
        {/* Header */}
        <div className="header">
          <h1>AI Job Application Assistant</h1>
          <p>Optimize your resume with AI-powered keyword matching and ATS scoring</p>
        </div>

        {/* Progress Stepper */}
        <div className="stepper">
          <Step number={1} label="Upload Resume" active={currentStep === 1} completed={currentStep > 1} />
          <Step number={2} label="Job Description" active={currentStep === 2} completed={currentStep > 2} />
          <Step number={3} label="Enhance" active={currentStep === 3} completed={currentStep > 3} />
          <Step number={4} label="Results" active={currentStep === 4} completed={false} />
        </div>

        {/* Error Display */}
        {error && (
          <div className="alert alert-error" style={{ marginBottom: '2rem' }}>
            {error}
          </div>
        )}

        {/* Step Content */}
        <div className="card">
          {isLoading ? (
            <LoadingIndicator />
          ) : (
            <>
              {/* Step 1: Upload Resume */}
              {currentStep === 1 && (
                <div>
                  <h2 className="card-title">Step 1: Upload Your Resume</h2>
                  <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                    Upload your current resume in PDF or DOCX format. We'll analyze your skills and experience.
                  </p>
                  <ResumeUploader
                    onFileSelect={setResumeFile}
                    selectedFile={resumeFile}
                    onClear={() => setResumeFile(null)}
                  />
                  <div className="actions">
                    <button
                      onClick={handleResumeUpload}
                      disabled={!resumeFile}
                      className="btn btn-primary"
                    >
                      Next: Job Description
                      <ArrowRight size={20} />
                    </button>
                  </div>
                </div>
              )}

              {/* Step 2: Job Description */}
              {currentStep === 2 && (
                <div>
                  <h2 className="card-title">Step 2: Enter Job Description</h2>
                  <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                    Paste the job description you're applying for. We'll extract key requirements and skills.
                  </p>
                  <JobDescriptionInput
                    onJobSubmit={handleJobDescriptionUpload}
                    jobData={jobData}
                    setJobData={setJobData}
                  />
                  <div className="actions">
                    <button
                      onClick={() => setCurrentStep(1)}
                      className="btn btn-secondary"
                    >
                      <ArrowLeft size={20} />
                      Back
                    </button>
                    <button
                      onClick={handleJobDescriptionUpload}
                      disabled={!jobData.descriptionText || jobData.descriptionText.length < 50}
                      className="btn btn-primary"
                    >
                      Next: Enhance Resume
                      <ArrowRight size={20} />
                    </button>
                  </div>
                </div>
              )}

              {/* Step 3: Enhance */}
              {currentStep === 3 && (
                <div>
                  <h2 className="card-title">Step 3: Enhance Your Resume</h2>
                  <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                    Our AI will analyze the job requirements and enhance your resume with:
                  </p>
                  <ul style={{ paddingLeft: '2rem', marginBottom: '2rem', lineHeight: '2' }}>
                    <li>Relevant keywords naturally integrated into your resume</li>
                    <li>Industry-relevant projects in Google XYZ format</li>
                    <li>Optimized formatting for ATS compatibility</li>
                    <li>Single-page professional layout</li>
                  </ul>
                  <div style={{
                    padding: '1.5rem',
                    background: 'var(--bg-secondary)',
                    borderRadius: 'var(--radius)',
                    marginBottom: '2rem'
                  }}>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                      <strong>Note:</strong> This process typically takes 5-10 seconds.
                      We use Claude AI to intelligently match your experience with job requirements.
                    </p>
                  </div>
                  <div className="actions">
                    <button
                      onClick={() => setCurrentStep(2)}
                      className="btn btn-secondary"
                    >
                      <ArrowLeft size={20} />
                      Back
                    </button>
                    <button
                      onClick={handleEnhanceResume}
                      className="btn btn-primary"
                      style={{ fontSize: '1.125rem', padding: '1rem 2rem' }}
                    >
                      <CheckCircle size={24} />
                      Enhance Resume with AI
                    </button>
                  </div>
                </div>
              )}

              {/* Step 4: Results */}
              {currentStep === 4 && enhancementResult && (
                <div>
                  <h2 className="card-title" style={{ textAlign: 'center', fontSize: '2rem', marginBottom: '1rem' }}>
                    🎉 Your Resume Has Been Enhanced!
                  </h2>
                  <p style={{ textAlign: 'center', color: 'var(--text-secondary)', marginBottom: '3rem' }}>
                    Processing completed in {(enhancementResult.processingTimeMs / 1000).toFixed(2)} seconds
                  </p>

                  {/* ATS Score Display */}
                  <div style={{ marginBottom: '3rem' }}>
                    <ATSScoreDisplay
                      originalScore={enhancementResult.originalAtsScore}
                      enhancedScore={enhancementResult.enhancedAtsScore}
                      scoreBreakdown={enhancementResult.scoreBreakdown}
                    />
                  </div>

                  {/* Enhanced Resume Viewer */}
                  <div style={{ marginBottom: '2rem' }}>
                    <EnhancedResumeViewer
                      sessionId={enhancementResult.sessionId}
                      addedKeywords={enhancementResult.addedKeywords}
                      addedProjects={enhancementResult.addedProjects}
                    />
                  </div>

                  {/* Actions */}
                  <div className="actions">
                    <button
                      onClick={handleStartOver}
                      className="btn btn-secondary"
                    >
                      <RefreshCw size={20} />
                      Enhance Another Resume
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        <div style={{ textAlign: 'center', marginTop: '3rem', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          <p>
            Powered by Claude AI • Built with Spring Boot & React •
            <a href="https://github.com" style={{ color: 'var(--primary-color)', marginLeft: '0.5rem' }}>
              View on GitHub
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}

/**
 * Step component for progress stepper
 */
const Step = ({ number, label, active, completed }) => {
  return (
    <div className={`step ${active ? 'active' : ''} ${completed ? 'completed' : ''}`}>
      <div className="step-circle">
        {completed ? <CheckCircle size={20} /> : number}
      </div>
      <div className="step-label">{label}</div>
    </div>
  );
};

export default App;
