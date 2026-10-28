import http, { cleanParams, unwrap } from './httpClient';

const partnerApi = (base) => ({
  search: (params) => unwrap(http.get(base, { params: cleanParams(params) })),
  get: (id) => unwrap(http.get(`${base}/${id}`)),
  create: (body) => unwrap(http.post(base, body)),
  update: (id, body) => unwrap(http.put(`${base}/${id}`, body)),
  deactivate: (id) => unwrap(http.delete(`${base}/${id}`)),
});

export const SupplierApi = partnerApi('/suppliers');
export const CustomerApi = partnerApi('/customers');
