import http, { cleanParams, unwrap } from './httpClient';

export const InventoryApi = {
  search: (params) => unwrap(http.get('/inventory', { params: cleanParams(params) })),
  byProduct: (warehouseId, productId) => unwrap(http.get('/inventory/by-product', { params: { warehouseId, productId } })),
  available: (warehouseId, productId) => unwrap(http.get('/inventory/available', { params: { warehouseId, productId } })),
  adjustments: (params) => unwrap(http.get('/inventory/adjustments', { params: cleanParams(params) })),
  createAdjustment: (body) => unwrap(http.post('/inventory/adjustments', body)),
  approveAdjustment: (id) => unwrap(http.post(`/inventory/adjustments/${id}/approve`)),
  rejectAdjustment: (id, reason) => unwrap(http.post(`/inventory/adjustments/${id}/reject`, { reason })),
  thresholds: (warehouseId) => unwrap(http.get('/inventory/thresholds', { params: cleanParams({ warehouseId }) })),
  saveThreshold: (body) => unwrap(http.post('/inventory/thresholds', body)),
  deleteThreshold: (id) => unwrap(http.delete(`/inventory/thresholds/${id}`)),

  // Warehouses and storage locations
  warehouses: (activeOnly = false) => unwrap(http.get('/warehouses', { params: { activeOnly } })),
  saveWarehouse: (id, body) => unwrap(id ? http.put(`/warehouses/${id}`, body) : http.post('/warehouses', body)),
  locations: (warehouseId) => unwrap(http.get(`/warehouses/${warehouseId}/locations`)),
  saveLocation: (warehouseId, id, body) =>
    unwrap(id ? http.put(`/warehouses/${warehouseId}/locations/${id}`, body)
      : http.post(`/warehouses/${warehouseId}/locations`, body)),
};
