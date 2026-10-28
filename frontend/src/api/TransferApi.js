import http, { cleanParams, unwrap } from './httpClient';

export const TransferApi = {
  search: (params) => unwrap(http.get('/transfers', { params: cleanParams(params) })),
  get: (id) => unwrap(http.get(`/transfers/${id}`)),
  create: (body) => unwrap(http.post('/transfers', body)),
  update: (id, body) => unwrap(http.put(`/transfers/${id}`, body)),
  submit: (id) => unwrap(http.post(`/transfers/${id}/submit`)),
  approve: (id) => unwrap(http.post(`/transfers/${id}/approve`)),
  reject: (id, reason) => unwrap(http.post(`/transfers/${id}/reject`, { reason })),
  cancel: (id) => unwrap(http.post(`/transfers/${id}/cancel`)),
  receive: (id) => unwrap(http.post(`/transfers/${id}/receive`)),
  defects: (params) => unwrap(http.get('/defects', { params: cleanParams(params) })),
  registerDefect: (body) => unwrap(http.post('/defects', body)),
  resolveDefect: (id, status, note) => unwrap(http.post(`/defects/${id}/resolve`, { status, note })),
};
