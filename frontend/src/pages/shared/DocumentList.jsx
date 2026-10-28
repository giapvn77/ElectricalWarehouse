import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import WarehouseSelect from '../../components/WarehouseSelect';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { RECEIPT_STATUS, toOptions } from '../../utils/constants';
import { formatDate, formatMoney } from '../../utils/format';

/** Filterable list of stock-in or stock-out receipts. extraColumns are inserted before the status. */
export default function DocumentList({ api, basePath, partnerKey, partnerTitle, fixedStatus, extraColumns = [] }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({ status: fixedStatus || '', warehouseId: null, keyword: '' });
  const [filters, setFilters] = useState({ status: fixedStatus });
  const fetcher = useCallback((p) => api.search(p), [api]);
  const { data, loading, error, setPage } = usePagedQuery(fetcher, filters);

  return (
    <div className="card">
      <form className="filters" onSubmit={(e) => { e.preventDefault(); setFilters({ ...form }); }}>
        <input placeholder="Mã phiếu" value={form.keyword} onChange={(e) => setForm({ ...form, keyword: e.target.value })} />
        <WarehouseSelect allowAll value={form.warehouseId} onChange={(v) => setForm({ ...form, warehouseId: v })} />
        {!fixedStatus && (
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            <option value="">Tất cả trạng thái</option>
            {toOptions(RECEIPT_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        )}
        <button className="btn">Lọc</button>
      </form>
      <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage}
        onRowClick={(r) => navigate(`${basePath}/${r.id}`)}
        columns={[
          { key: 'code', title: 'Mã phiếu' },
          { key: 'receiptDate', title: 'Ngày', render: (r) => formatDate(r.receiptDate) },
          { key: 'warehouseName', title: 'Kho' },
          { key: partnerKey, title: partnerTitle },
          ...extraColumns,
          { key: 'totalAmount', title: 'Tổng tiền', align: 'right', render: (r) => formatMoney(r.totalAmount) },
          { key: 'createdByName', title: 'Người lập' },
          { key: 'status', title: 'Trạng thái', render: (r) => <StatusBadge status={r.status} /> },
        ]} />
    </div>
  );
}
