import http, { unwrap } from './httpClient';

export const AuthApi = {
  login: (username, password) => unwrap(http.post('/auth/login', { username, password })),
  logout: (refreshToken) => unwrap(http.post('/auth/logout', { refreshToken })),
  me: () => unwrap(http.get('/auth/me')),
  changePassword: (body) => unwrap(http.post('/auth/change-password', body)),
};
