import axios from 'axios';
import { tokenStorage } from '../utils/tokenStorage';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const http = axios.create({ baseURL: API_BASE_URL });

http.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// One refresh at a time; concurrent 401s wait for the same promise.
let refreshing = null;
let onSessionExpired = () => {};

export function setSessionExpiredHandler(handler) {
  onSessionExpired = handler;
}

async function refreshTokens() {
  const refreshToken = tokenStorage.getRefresh();
  if (!refreshToken) throw new Error('No refresh token');
  const res = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken });
  tokenStorage.save(res.data.data);
  return res.data.data;
}

http.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    const isAuthCall = original?.url?.startsWith('/auth/');
    if (error.response?.status === 401 && original && !original._retry && !isAuthCall) {
      original._retry = true;
      try {
        refreshing = refreshing || refreshTokens();
        await refreshing;
        return http(original);
      } catch {
        tokenStorage.clear();
        onSessionExpired();
      } finally {
        refreshing = null;
      }
    }
    return Promise.reject(error);
  },
);

/** Unwraps ApiResponse<T> -> T. */
export const unwrap = (promise) => promise.then((res) => res.data.data);

/** Strips empty values so they are not sent as "" query parameters. */
export const cleanParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined));

export async function getFile(url, params) {
  const res = await http.get(url, { params: cleanParams(params), responseType: 'blob' });
  const disposition = res.headers['content-disposition'] || '';
  const match = /filename="?([^";]+)"?/.exec(disposition);
  return { blob: res.data, fileName: match ? match[1] : 'export' };
}

export default http;
