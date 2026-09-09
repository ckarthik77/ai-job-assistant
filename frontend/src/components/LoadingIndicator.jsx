import React, { useState, useEffect } from 'react';
import { Loader2 } from 'lucide-react';

/**
 * Loading indicator with progress timer
 */
const LoadingIndicator = () => {
  const [elapsed, setElapsed] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setElapsed(prev => prev + 0.1);
    }, 100);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="loading">
      <div className="spinner" />
      <p className="loading-text">Enhancing your resume with AI...</p>
      <p className="loading-timer">
        {elapsed.toFixed(1)}s elapsed
      </p>
      <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: '1rem' }}>
        • Analyzing job requirements<br />
        • Matching skills and keywords<br />
        • Selecting relevant projects<br />
        • Formatting in Google XYZ style<br />
        • Generating enhanced resume
      </p>
    </div>
  );
};

export default LoadingIndicator;
