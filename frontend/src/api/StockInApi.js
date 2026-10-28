import http, { cleanParams, unwrap } from './httpClient';

export const StockInApi = {
  search: (params) => unwrap(http.get('/stock-in', { params: cleanParams(params) })),
  get: (id) => unwrap(http.get(`/stock-in/${id}`)),
  create: (body) => unwrap(http.post('/stock-in', body)),
  update: (id, body) => unwrap(http.put(`/stock-in/${id}`, body)),
  submit: (id) => unwrap(http.post(`/stock-in/${id}/submit`)),
  approve: (id) => unwrap(http.post(`/stock-in/${id}/approve`)),
  reject: (id, reason) => unwrap(http.post(`/stock-in/${id}/reject`, { reason })),
  cancel: (id) => unwrap(http.post(`/stock-in/${id}/cancel`)),
  complete: (id) => unwrap(http.post(`/stock-in/${id}/complete`)),
  scanSerials: (id, detailId, serialNumbers) =>
    unwrap(http.post(`/stock-in/${id}/serials`, { detailId, serialNumbers })),
  removeSerial: (id, serialId) => unwrap(http.delete(`/stock-in/${id}/serials/${serialId}`)),
  serialsOfDetail: (detailId) => unwrap(http.get(`/stock-in/details/${detailId}/serials`)),
  lookupSerial: (serialNumber) => unwrap(http.get(`/serials/${encodeURIComponent(serialNumber)}`)),
};
