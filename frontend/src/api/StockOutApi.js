import http, { cleanParams, unwrap } from './httpClient';

export const StockOutApi = {
  search: (params) => unwrap(http.get('/stock-out', { params: cleanParams(params) })),
  get: (id) => unwrap(http.get(`/stock-out/${id}`)),
  create: (body) => unwrap(http.post('/stock-out', body)),
  update: (id, body) => unwrap(http.put(`/stock-out/${id}`, body)),
  submit: (id) => unwrap(http.post(`/stock-out/${id}/submit`)),
  approve: (id) => unwrap(http.post(`/stock-out/${id}/approve`)),
  reject: (id, reason) => unwrap(http.post(`/stock-out/${id}/reject`, { reason })),
  cancel: (id) => unwrap(http.post(`/stock-out/${id}/cancel`)),
  complete: (id) => unwrap(http.post(`/stock-out/${id}/complete`)),
  pickList: (id) => unwrap(http.get(`/stock-out/${id}/pick-list`)),
  pick: (id, body) => unwrap(http.post(`/stock-out/${id}/pick`, body)),
};
