import http, { cleanParams, unwrap } from './httpClient';

export const ProductApi = {
  search: (params) => unwrap(http.get('/products', { params: cleanParams(params) })),
  get: (id) => unwrap(http.get(`/products/${id}`)),
  lookup: (code) => unwrap(http.get('/products/lookup', { params: { code } })),
  create: (body) => unwrap(http.post('/products', body)),
  update: (id, body) => unwrap(http.put(`/products/${id}`, body)),
  deactivate: (id) => unwrap(http.delete(`/products/${id}`)),
  uploadImage: (id, file) => {
    const form = new FormData();
    form.append('file', file);
    return unwrap(http.post(`/products/${id}/images`, form));
  },
  barcodeUrl: (id) => `/products/${id}/barcode`,
  categories: () => unwrap(http.get('/categories')),
  saveCategory: (id, body) => unwrap(id ? http.put(`/categories/${id}`, body) : http.post('/categories', body)),
  units: () => unwrap(http.get('/units')),
  saveUnit: (id, body) => unwrap(id ? http.put(`/units/${id}`, body) : http.post('/units', body)),
};
