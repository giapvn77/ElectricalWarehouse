const numberFmt = new Intl.NumberFormat('vi-VN');
const moneyFmt = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 });

export const formatNumber = (v) => (v == null ? '' : numberFmt.format(v));
export const formatMoney = (v) => (v == null ? '' : moneyFmt.format(v));
export const formatDate = (v) => (v ? new Date(v).toLocaleDateString('vi-VN') : '');
export const formatDateTime = (v) => (v ? new Date(v).toLocaleString('vi-VN') : '');
// yyyy-MM-dd in the user's local time zone. toISOString() would give the UTC date,
// which in Vietnam (UTC+7) is still "yesterday" between 00:00 and 07:00.
const isoLocalDate = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
export const today = () => isoLocalDate(new Date());
export const firstDayOfMonth = () => {
  const d = new Date();
  return isoLocalDate(new Date(d.getFullYear(), d.getMonth(), 1));
};

export function getErrorMessage(error) {
  return error?.response?.data?.message || error?.message || 'Đã có lỗi xảy ra';
}

/** Field errors returned by the API for invalid forms: { field: message }. */
export function getFieldErrors(error) {
  const data = error?.response?.data?.data;
  return data && typeof data === 'object' && !Array.isArray(data) ? data : {};
}

export function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
