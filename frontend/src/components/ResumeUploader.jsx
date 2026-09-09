import React, { useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, File, X } from 'lucide-react';

/**
 * Resume file uploader component with drag-and-drop
 */
const ResumeUploader = ({ onFileSelect, selectedFile, onClear }) => {
  const onDrop = useCallback((acceptedFiles) => {
    if (acceptedFiles.length > 0) {
      onFileSelect(acceptedFiles[0]);
    }
  }, [onFileSelect]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx']
    },
    maxFiles: 1,
    multiple: false
  });

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  return (
    <div>
      {!selectedFile ? (
        <div
          {...getRootProps()}
          className={`dropzone ${isDragActive ? 'active' : ''}`}
        >
          <input {...getInputProps()} />
          <div className="dropzone-icon">
            <Upload size={48} />
          </div>
          <p className="dropzone-text">
            {isDragActive
              ? 'Drop your resume here'
              : 'Drag and drop your resume, or click to browse'}
          </p>
          <p className="dropzone-hint">
            Supports PDF and DOCX files (max 10MB)
          </p>
        </div>
      ) : (
        <div className="file-preview">
          <File size={32} className="file-preview-icon" />
          <div className="file-preview-info">
            <div className="file-preview-name">{selectedFile.name}</div>
            <div className="file-preview-size">
              {formatFileSize(selectedFile.size)}
            </div>
          </div>
          <button
            onClick={onClear}
            className="btn btn-secondary"
            style={{ padding: '0.5rem' }}
          >
            <X size={20} />
          </button>
        </div>
      )}
    </div>
  );
};

export default ResumeUploader;
