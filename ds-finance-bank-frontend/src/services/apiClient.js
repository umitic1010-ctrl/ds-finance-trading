import axios from 'axios';
import { API_BASE_URL } from '../config/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true,
});

export const createAuthClient = (username, password) =>
  axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Basic ${btoa(`${username}:${password}`)}`,
    },
    withCredentials: true,
  });

// Request Interceptor - fügt Auth Header hinzu
apiClient.interceptors.request.use(
  (config) => {
    const auth = localStorage.getItem('auth');
    if (auth) {
      try {
        const authData = JSON.parse(auth);
        // Check if we have the password stored (from login)
        if (authData.password) {
          config.headers.Authorization = `Basic ${btoa(`${authData.username}:${authData.password}`)}`;
        } else {
          console.warn('No password in auth data - user needs to login again');
        }
      } catch (e) {
        console.error('Error parsing auth data:', e);
        localStorage.removeItem('auth');
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor - behandelt Fehler
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
