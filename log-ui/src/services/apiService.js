
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
});

const handleError = (functionName, error) => {
  console.error(`Error in ${functionName}:`, error.response?.data || error.message);
  throw error;
};


const processSearchResponse = (response) => {
  if (Array.isArray(response.data?.hits)) {
    return response.data.hits;
  }

  if (response.data && response.data.id) {
    return [response.data];
  }

  return [];
};


export const searchByEventId = async (id) => {
    try {
        const response = await apiClient.get(`/search/v1/public/event/${id}`);
        return processSearchResponse(response);
    } catch (error) { handleError('searchByEventId', error); return []; }
};

export const searchBySubject = async (subjectType, subjectId) => {
    try {
        const response = await apiClient.get(`/search/v1/public/subject`, { params: { subjectType, subjectId } });
        return processSearchResponse(response);
    } catch (error) { handleError('searchBySubject', error); return []; }
};

export const searchByTarget = async (targetType, targetId) => {
    try {
        const response = await apiClient.get(`/search/v1/public/target`, { params: { targetType, targetId } });
        return processSearchResponse(response);
    } catch (error) { handleError('searchByTarget', error); return []; }
};

export const searchByEventType = async (eventType) => {
    try {
        const response = await apiClient.get(`/search/v1/public/event-type/${eventType}`);
        return processSearchResponse(response);
    } catch (error) { handleError('searchByEventType', error); return []; }
};

export const searchByCorrelationId = async (correlationId) => {
    try {
        const response = await apiClient.get(`/search/v1/public/correlation/${correlationId}`);
        return processSearchResponse(response);
    } catch (error) { handleError('searchByCorrelationId', error); return []; }
};

export const searchFullText = async (query) => {
    try {
        const response = await apiClient.get(`/search/v1/public/full-text`, { params: { q: query } });
        return processSearchResponse(response);
    } catch (error) { handleError('searchFullText', error); return []; }
};

export const searchByTimeRange = async (fromTime, toTime) => {
    try {
        const response = await apiClient.get(`/search/v1/public/time-range`, { params: { fromTime, toTime } });
        return processSearchResponse(response);
    } catch (error) { handleError('searchByTimeRange', error); return []; }
};


export const fetchTypeStatisticsByTime = async (params) => {
    try {
        const response = await apiClient.get('/search/v1/public/stats/types-by-time', { params });
        return response.data || { subjectTypes: [], targetTypes: [] };
    } catch (error) {
        handleError('fetchTypeStatisticsByTime', error);
        return { subjectTypes: [], targetTypes: [] };
    }
};