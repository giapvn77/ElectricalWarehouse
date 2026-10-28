import { useState } from 'react';
import { ReportApi } from '../../api/ReportApi';
import DataTable from '../../components/DataTable';
import PageHeader from '../../components/PageHeader';
import WarehouseSelect from '../../components/WarehouseSelect';
import { useAsync } from '../../hooks/useAsync';
import { firstDayOfMonth, formatNumber, today } from '../../utils/format';
import ExportModal from './ExportModal';

/** Nhập - Xuất - Tồn: opening + in - out = closing for each product in the period. */
export default function XNTReportPage() {
  const [form, setForm] = useState({ warehouseId: null, fromDate: firstDayOfMonth(), toDate: today() });
  const [filters, setFilters] = useState(form);
  const [exporting, setExporting] = useState(false);
  const { data, loading, error } = useAsync(() => ReportApi.xnt(filters), [filters]);

  const totals = (data?.rows || []).reduce((t, r) => ({
    opening: t.opening + r.openingQuantity, in: t.in + r.inQuantity, out: t.out + r.outQuantity, closing: t.closing + r.closingQuantity,
  }), { opening: 0, in: 0, out: 0, closing: 0 });

  return (
    <>
      <PageHeader title="Báo cáo Nhập - Xuất - Tồn" actions={<button className="btn" onClick={() => setExporting(true)}>Xuất file</button>} />
      <div className="card">
        <form className="filters" onSubmit={(e) => { e.preventDefault(); setFilters({ ...form }); }}>
          <WarehouseSelect allowAll value={form.warehouseId} onChange={(v) => setForm({ ...form, warehouseId: v })} />
          <input type="date" value={form.fromDate} onChange={(e) => setForm({ ...form, fromDate: e.target.value })} />
          <input type="date" value={form.toDate} onChange={(e) => setForm({ ...form, toDate: e.target.value })} />
          <button className="btn">Xem</button>
        </form>
        <DataTable loading={loading} error={error} rows={data?.rows || []} rowKey="productId"
          columns={[
            { key: 'sku', title: 'Mã hàng' },
            { key: 'productName', title: 'Tên hàng' },
            { key: 'unitName', title: 'ĐVT' },
            { key: 'openingQuantity', title: 'Tồn đầu', align: 'right', render: (r) => formatNumber(r.openingQuantity) },
            { key: 'inQuantity', title: 'Nhập', align: 'right', render: (r) => formatNumber(r.inQuantity) },
            { key: 'outQuantity', title: 'Xuất', align: 'right', render: (r) => formatNumber(r.outQuantity) },
            { key: 'closingQuantity', title: 'Tồn cuối', align: 'right', render: (r) => <b>{formatNumber(r.closingQuantity)}</b> },
          ]} />
        {data?.rows?.length > 0 && (
          <p className="right"><b>Tổng:</b> tồn đầu {formatNumber(totals.opening)} · nhập {formatNumber(totals.in)} · xuất {formatNumber(totals.out)} · tồn cuối {formatNumber(totals.closing)}</p>
        )}
      </div>
      <ExportModal open={exporting} onClose={() => setExporting(false)} report="xnt" params={filters} />
    </>
  );
}
