import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

/**
 * Upload resume file
 */
export const uploadResume = async (file, email = null) => {
  const formData = new FormData();
  formData.append('file', file);
  if (email) {
    formData.append('email', email);
  }

  const response = await apiClient.post('/resume/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};

/**
 * Upload job description
 */
export const uploadJobDescription = async (jobData) => {
  const response = await apiClient.post('/job/upload', jobData);
  return response.data;
};

/**
 * Enhance resume
 */
export const enhanceResume = async (resumeId, jobDescriptionId) => {
  const response = await apiClient.post('/enhance', null, {
    params: {
      resumeId,
      jobDescriptionId,
    },
  });
  return response.data;
};

/**
 * Get enhancement session details
 */
export const getEnhancementSession = async (sessionId) => {
  const response = await apiClient.get(`/enhanced/${sessionId}`);
  return response.data;
};

/**
 * Get download URL for enhanced resume
 */
export const getDownloadUrl = (sessionId) => {
  return `${API_BASE_URL}/enhanced/${sessionId}/download`;
};

/**
 * Get all available projects
 */
export const getProjects = async (category = null) => {
  const response = await apiClient.get('/projects', {
    params: category ? { category } : {},
  });
  return response.data;
};

/**
 * Health check
 */
export const healthCheck = async () => {
  const response = await apiClient.get('/health');
  return response.data;
};

export default apiClient;
