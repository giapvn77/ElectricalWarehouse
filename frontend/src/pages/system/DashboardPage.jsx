import { useNavigate } from 'react-router-dom';
import { ReportApi } from '../../api/ReportApi';
import DataTable from '../../components/DataTable';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import { useAsync } from '../../hooks/useAsync';
import { REFERENCE_TYPE } from '../../utils/constants';
import { formatDateTime, formatNumber } from '../../utils/format';

export default function DashboardPage() {
  const navigate = useNavigate();
  const summary = useAsync(() => ReportApi.dashboardSummary(), []);
  const pending = useAsync(() => ReportApi.pendingApprovals(), []);
  const s = summary.data;
  const p = pending.data;

  const cards = [
    { label: 'Sản phẩm', value: s?.productCount },
    { label: 'Kho', value: s?.warehouseCount },
    { label: 'Tổng tồn kho', value: s?.totalStockQuantity },
    { label: 'Dưới định mức', value: s?.lowStockCount, tone: s?.lowStockCount ? 'red' : undefined },
    { label: 'Hàng lỗi chờ xử lý', value: s?.openDefectCount },
    { label: 'Phiếu chờ duyệt', value: p?.total, tone: p?.total ? 'amber' : undefined },
  ];

  return (
    <>
      <PageHeader title="Tổng quan" />
      <div className="stats">
        {cards.map((c) => (
          <div key={c.label} className={`stat ${c.tone ? 'stat-' + c.tone : ''}`}>
            <div className="stat-value">{c.value == null ? '…' : formatNumber(c.value)}</div>
            <div className="stat-label">{c.label}</div>
          </div>
        ))}
      </div>

      <div className="card">
        <h2>Phiếu chờ duyệt</h2>
        {p && (
          <p className="muted">
            Nhập: {p.stockInCount} · Xuất: {p.stockOutCount} · Chuyển kho: {p.transferCount} · Kiểm kê: {p.adjustmentCount}
          </p>
        )}
        <DataTable
          loading={pending.loading}
          error={pending.error}
          rows={p?.items || []}
          rowKey="code"
          onRowClick={(r) => navigate(r.type === 'STOCK_IN' || r.type === 'STOCK_OUT' ? `${REFERENCE_TYPE[r.type].path}/${r.id}` : REFERENCE_TYPE[r.type].path)}
          columns={[
            { key: 'type', title: 'Loại', render: (r) => REFERENCE_TYPE[r.type]?.label },
            { key: 'code', title: 'Mã phiếu' },
            { key: 'warehouseName', title: 'Kho' },
            { key: 'createdByName', title: 'Người lập' },
            { key: 'createdAt', title: 'Ngày lập', render: (r) => formatDateTime(r.createdAt) },
            { key: 'status', title: 'Trạng thái', render: (r) => <StatusBadge status={r.status} /> },
          ]}
        />
      </div>
    </>
  );
}
