import { useCallback, useEffect, useState } from 'react';
import { InventoryApi } from '../../api/InventoryApi';
import DataTable from '../../components/DataTable';
import PageHeader from '../../components/PageHeader';
import WarehouseSelect from '../../components/WarehouseSelect';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { formatDate, formatDateTime, formatNumber } from '../../utils/format';

const REFRESH_MS = 30_000;

export default function InventoryRealTimePage() {
  const [form, setForm] = useState({ warehouseId: null, keyword: '' });
  const [filters, setFilters] = useState({});
  const [updatedAt, setUpdatedAt] = useState(new Date());
  const fetcher = useCallback((p) => InventoryApi.search(p), []);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters, 50);

  useEffect(() => {
    const timer = setInterval(() => {
      reload();
      setUpdatedAt(new Date());
    }, REFRESH_MS);
    return () => clearInterval(timer);
  }, [reload]);

  return (
    <>
      <PageHeader title="Tồn kho thời gian thực" subtitle={`Tự động cập nhật mỗi 30 giây · lần cuối ${formatDateTime(updatedAt)}`} />
      <div className="card">
        <form className="filters" onSubmit={(e) => { e.preventDefault(); setFilters({ ...form }); }}>
          <WarehouseSelect allowAll value={form.warehouseId} onChange={(v) => setForm({ ...form, warehouseId: v })} />
          <input placeholder="Mã hoặc tên hàng" value={form.keyword} onChange={(e) => setForm({ ...form, keyword: e.target.value })} />
          <button className="btn">Lọc</button>
        </form>
        <DataTable loading={loading && data.content.length === 0} error={error} rows={data.content} page={data} onPageChange={setPage}
          columns={[
            { key: 'warehouseName', title: 'Kho' },
            { key: 'sku', title: 'Mã hàng' },
            { key: 'productName', title: 'Tên hàng' },
            { key: 'locationCode', title: 'Vị trí' },
            { key: 'lotNumber', title: 'Lô' },
            { key: 'expiryDate', title: 'Hạn dùng', render: (r) => formatDate(r.expiryDate) },
            { key: 'quantity', title: 'Tồn', align: 'right', render: (r) => formatNumber(r.quantity) },
            { key: 'reservedQuantity', title: 'Giữ chỗ', align: 'right', render: (r) => formatNumber(r.reservedQuantity) },
            { key: 'availableQuantity', title: 'Khả dụng', align: 'right', render: (r) => <b>{formatNumber(r.availableQuantity)}</b> },
            { key: 'unitName', title: 'ĐVT' },
          ]} />
      </div>
    </>
  );
}
