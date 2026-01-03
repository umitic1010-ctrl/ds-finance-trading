import axios from 'axios';
import { API_BASE_URL } from '../config/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true,
});

const BASIC_CREDENTIALS = {
  employee: {
    user: process.env.REACT_APP_BASIC_EMPLOYEE_USER,
    pass: process.env.REACT_APP_BASIC_EMPLOYEE_PASS,
  },
  customer: {
    user: process.env.REACT_APP_BASIC_CUSTOMER_USER,
    pass: process.env.REACT_APP_BASIC_CUSTOMER_PASS,
  },
};

// Request Interceptor - fügt Auth Header hinzu
apiClient.interceptors.request.use(
  (config) => {
    const auth = localStorage.getItem('auth');
    if (auth) {
      try {
        const authData = JSON.parse(auth);
        if (authData.token) {
          config.headers['X-Auth-Token'] = authData.token;
        }
        const role = authData.role;
        const basic = BASIC_CREDENTIALS[role];
        if (basic?.user && basic?.pass) {
          config.headers.Authorization = `Basic ${btoa(`${basic.user}:${basic.pass}`)}`;
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
