import { useCallback, useState } from 'react';
import { ReportApi } from '../../api/ReportApi';
import DataTable from '../../components/DataTable';
import PageHeader from '../../components/PageHeader';
import WarehouseSelect from '../../components/WarehouseSelect';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { TRANSACTION_TYPE, toOptions } from '../../utils/constants';
import { firstDayOfMonth, formatDateTime, formatNumber, today } from '../../utils/format';
import ExportModal from './ExportModal';

export default function MovementReportPage() {
  const [form, setForm] = useState({ warehouseId: null, type: '', fromDate: firstDayOfMonth(), toDate: today() });
  const [filters, setFilters] = useState(form);
  const [exporting, setExporting] = useState(false);
  const fetcher = useCallback((p) => ReportApi.movement(p), []);
  const { data, loading, error, setPage } = usePagedQuery(fetcher, filters, 50);

  return (
    <>
      <PageHeader title="Biến động kho" actions={<button className="btn" onClick={() => setExporting(true)}>Xuất file</button>} />
      <div className="card">
        <form className="filters" onSubmit={(e) => { e.preventDefault(); setFilters({ ...form }); }}>
          <WarehouseSelect allowAll value={form.warehouseId} onChange={(v) => setForm({ ...form, warehouseId: v })} />
          <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="">Tất cả loại</option>
            {toOptions(TRANSACTION_TYPE).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <input type="date" value={form.fromDate} onChange={(e) => setForm({ ...form, fromDate: e.target.value })} />
          <input type="date" value={form.toDate} onChange={(e) => setForm({ ...form, toDate: e.target.value })} />
          <button className="btn">Xem</button>
        </form>
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage}
          columns={[
            { key: 'transactionDate', title: 'Thời gian', render: (t) => formatDateTime(t.transactionDate) },
            { key: 'type', title: 'Loại', render: (t) => TRANSACTION_TYPE[t.type] },
            { key: 'warehouseName', title: 'Kho' },
            { key: 'sku', title: 'Mã hàng' },
            { key: 'productName', title: 'Tên hàng' },
            { key: 'locationCode', title: 'Vị trí' },
            { key: 'lotNumber', title: 'Lô' },
            { key: 'quantity', title: 'SL', align: 'right', render: (t) => <b className={t.quantity < 0 ? 'error' : 'ok'}>{t.quantity > 0 ? '+' : ''}{formatNumber(t.quantity)}</b> },
            { key: 'balanceAfter', title: 'Tồn sau', align: 'right', render: (t) => formatNumber(t.balanceAfter) },
            { key: 'referenceCode', title: 'Chứng từ' },
            { key: 'performedByName', title: 'Người thực hiện' },
          ]} />
      </div>
      <ExportModal open={exporting} onClose={() => setExporting(false)} report="movement" params={filters} />
    </>
  );
}
