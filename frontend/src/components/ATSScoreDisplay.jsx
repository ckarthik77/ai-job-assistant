import React from 'react';
import { TrendingUp, Award } from 'lucide-react';

/**
 * ATS Score display component showing before/after scores
 */
const ATSScoreDisplay = ({ originalScore, enhancedScore, scoreBreakdown }) => {
  const improvement = enhancedScore - originalScore;
  const improvementPercent = originalScore > 0
    ? Math.round((improvement / originalScore) * 100)
    : 0;

  return (
    <div>
      <h3 className="card-title" style={{ textAlign: 'center', marginBottom: '2rem' }}>
        <Award size={24} style={{ display: 'inline', marginRight: '0.5rem', verticalAlign: 'middle' }} />
        ATS Compatibility Score
      </h3>

      <div className="score-container">
        <div className="score-card">
          <div className="score-label">Original Score</div>
          <div className="score-value" style={{ color: originalScore >= 70 ? 'var(--secondary-color)' : 'var(--danger-color)' }}>
            {originalScore}
          </div>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>out of 100</div>
        </div>

        <div className="score-card">
          <div className="score-label">Enhanced Score</div>
          <div className="score-value" style={{ color: 'var(--secondary-color)' }}>
            {enhancedScore}
          </div>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>out of 100</div>
          {improvement > 0 && (
            <div className="score-improvement">
              <TrendingUp size={16} />
              +{improvement} points ({improvementPercent > 0 ? `+${improvementPercent}%` : '0%'})
            </div>
          )}
        </div>
      </div>

      {scoreBreakdown && (
        <div style={{ marginTop: '2rem' }}>
          <h4 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem' }}>
            Score Breakdown
          </h4>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
            <ScoreBreakdownItem
              label="Keyword Match"
              score={scoreBreakdown.keywordScore}
              max={scoreBreakdown.keywordMax}
            />
            <ScoreBreakdownItem
              label="Skill Coverage"
              score={scoreBreakdown.skillScore}
              max={scoreBreakdown.skillMax}
            />
            <ScoreBreakdownItem
              label="Format Compliance"
              score={scoreBreakdown.formatScore}
              max={scoreBreakdown.formatMax}
            />
            <ScoreBreakdownItem
              label="XYZ Format"
              score={scoreBreakdown.xyzScore}
              max={scoreBreakdown.xyzMax}
            />
          </div>
        </div>
      )}
    </div>
  );
};

const ScoreBreakdownItem = ({ label, score, max }) => {
  const percentage = (score / max) * 100;

  return (
    <div style={{ padding: '1rem', background: 'var(--bg-secondary)', borderRadius: 'var(--radius)' }}>
      <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
        {label}
      </div>
      <div style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '0.5rem' }}>
        {score}/{max}
      </div>
      <div style={{ width: '100%', height: '6px', background: 'var(--border-color)', borderRadius: '999px', overflow: 'hidden' }}>
        <div
          style={{
            width: `${percentage}%`,
            height: '100%',
            background: percentage >= 80 ? 'var(--secondary-color)' : percentage >= 50 ? 'var(--primary-color)' : 'var(--danger-color)',
            transition: 'width 0.5s'
          }}
        />
      </div>
    </div>
  );
};

export default ATSScoreDisplay;
