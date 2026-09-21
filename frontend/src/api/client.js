import axios from 'axios';

const TOKEN_KEY = 'syf_token';

// Base URL comes from the environment for production builds. In development it
// stays empty so requests hit the Vite dev proxy on the same origin.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || ''
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function apiError(error, fallback) {
  return error?.response?.data?.message || fallback || 'Something went wrong. Try again.';
}

export default api;
