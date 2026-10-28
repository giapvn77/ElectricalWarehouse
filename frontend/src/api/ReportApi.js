import http, { cleanParams, getFile, unwrap } from './httpClient';

export const ReportApi = {
  inventory: (warehouseId) => unwrap(http.get('/reports/inventory', { params: cleanParams({ warehouseId }) })),
  movement: (params) => unwrap(http.get('/reports/movement', { params: cleanParams(params) })),
  xnt: (params) => unwrap(http.get('/reports/xnt', { params: cleanParams(params) })),
  /** report: 'inventory' | 'movement' | 'xnt'; format: 'EXCEL' | 'PDF' */
  exportFile: (report, params, format) => getFile(`/reports/${report}/export`, { ...params, format }),

  dashboardSummary: () => unwrap(http.get('/dashboard/summary')),
  pendingApprovals: () => unwrap(http.get('/dashboard/pending-approvals')),

  notifications: (params) => unwrap(http.get('/notifications', { params: cleanParams(params) })),
  unreadCount: () => unwrap(http.get('/notifications/unread-count')),
  markRead: (id) => unwrap(http.post(`/notifications/${id}/read`)),
  markAllRead: () => unwrap(http.post('/notifications/read-all')),
};
