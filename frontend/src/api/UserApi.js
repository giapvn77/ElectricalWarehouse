import http, { cleanParams, unwrap } from './httpClient';

export const UserApi = {
  search: (params) => unwrap(http.get('/users', { params: cleanParams(params) })),
  get: (id) => unwrap(http.get(`/users/${id}`)),
  create: (body) => unwrap(http.post('/users', body)),
  update: (id, body) => unwrap(http.put(`/users/${id}`, body)),
  setActive: (id, value) => unwrap(http.patch(`/users/${id}/active`, null, { params: { value } })),
  roles: () => unwrap(http.get('/roles')),
  permissions: () => unwrap(http.get('/roles/permissions')),
  updateRolePermissions: (roleId, permissionCodes) =>
    unwrap(http.put(`/roles/${roleId}/permissions`, { permissionCodes })),
  auditLogs: (params) => unwrap(http.get('/dashboard/audit-logs', { params: cleanParams(params) })),
};
