export const RECEIPT_STATUS = {
  DRAFT: { label: 'Nháp', tone: 'gray' },
  PENDING: { label: 'Chờ duyệt', tone: 'amber' },
  APPROVED: { label: 'Đã duyệt', tone: 'blue' },
  REJECTED: { label: 'Từ chối', tone: 'red' },
  PICKING: { label: 'Đang lấy hàng', tone: 'violet' },
  COMPLETED: { label: 'Hoàn tất', tone: 'green' },
  CANCELLED: { label: 'Đã hủy', tone: 'gray' },
};

export const DEFECT_STATUS = {
  REPORTED: { label: 'Mới ghi nhận', tone: 'amber' },
  IN_DEFECTIVE_WAREHOUSE: { label: 'Trong kho lỗi', tone: 'red' },
  REPAIRED: { label: 'Đã sửa', tone: 'green' },
  RETURNED_TO_SUPPLIER: { label: 'Trả NCC', tone: 'blue' },
  DISPOSED: { label: 'Tiêu hủy', tone: 'gray' },
};

export const DEFECT_SOURCE = {
  RECEIVING: 'Phát hiện khi nhập',
  STORAGE: 'Hỏng trong kho',
  CUSTOMER_RETURN: 'Khách trả lại',
  WARRANTY: 'Bảo hành',
};

export const OUT_TYPE = {
  SALE: 'Bán hàng',
  TRANSFER: 'Chuyển nội bộ',
  WARRANTY: 'Bảo hành',
  DISPOSE: 'Thanh lý',
  OTHER: 'Khác',
};

export const WAREHOUSE_TYPE = { MAIN: 'Kho chính', BRANCH: 'Kho chi nhánh', DEFECTIVE: 'Kho hàng lỗi' };

export const TRANSACTION_TYPE = {
  IN: 'Nhập',
  OUT: 'Xuất',
  ADJUST: 'Điều chỉnh',
  TRANSFER_IN: 'Chuyển đến',
  TRANSFER_OUT: 'Chuyển đi',
  DEFECT: 'Hàng lỗi',
};

export const ROLES = {
  ADMIN: 'Quản trị hệ thống',
  QLK: 'Quản lý kho',
  NVTM: 'Nhân viên thu mua',
  NVK: 'Nhân viên kho',
  NVBH: 'Nhân viên bán hàng',
};

export const REFERENCE_TYPE = {
  STOCK_IN: { label: 'Phiếu nhập', path: '/stock-in' },
  STOCK_OUT: { label: 'Phiếu xuất', path: '/stock-out' },
  TRANSFER: { label: 'Chuyển kho', path: '/transfers' },
  ADJUSTMENT: { label: 'Kiểm kê', path: '/inventory/adjustments' },
  DEFECT: { label: 'Hàng lỗi', path: '/defects' },
};

export const toOptions = (map) =>
  Object.entries(map).map(([value, v]) => ({ value, label: typeof v === 'string' ? v : v.label }));
