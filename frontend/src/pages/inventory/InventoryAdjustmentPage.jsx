import { useCallback, useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { InventoryApi } from '../../api/InventoryApi';
import ConfirmDialog from '../../components/ConfirmDialog';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import ProductPicker from '../../components/ProductPicker';
import StatusBadge from '../../components/StatusBadge';
import WarehouseSelect from '../../components/WarehouseSelect';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { useToast } from '../../hooks/useToast';
import { hasPermission, hasRole, selectUser } from '../../store/AuthStore';
import { formatDateTime, getErrorMessage } from '../../utils/format';

/** Stock count results. The difference is applied only after a manager approves. */
export default function InventoryAdjustmentPage() {
  const user = useSelector(selectUser);
  const toast = useToast();
  const [filters, setFilters] = useState({});
  const fetcher = useCallback((p) => InventoryApi.adjustments(p), []);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters);
  const [creating, setCreating] = useState(false);
  const [deciding, setDeciding] = useState(null); // { doc, action }

  const decide = async (reason) => {
    try {
      if (deciding.action === 'approve') await InventoryApi.approveAdjustment(deciding.doc.id);
      else await InventoryApi.rejectAdjustment(deciding.doc.id, reason);
      toast.success('Đã xử lý phiếu kiểm kê');
      setDeciding(null);
      reload();
    } catch (e) {
      toast.error(e);
    }
  };

  const isManager = hasRole(user, 'ADMIN', 'QLK');

  return (
    <>
      <PageHeader title="Kiểm kê & điều chỉnh tồn"
        actions={hasPermission(user, 'INVENTORY_ADJUST') && <button className="btn btn-primary" onClick={() => setCreating(true)}>+ Phiếu kiểm kê</button>} />
      <div className="card">
        <div className="filters">
          <WarehouseSelect allowAll value={filters.warehouseId} onChange={(v) => setFilters({ ...filters, warehouseId: v })} />
          <select value={filters.status || ''} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
            <option value="">Tất cả</option>
            <option value="PENDING">Chờ duyệt</option>
            <option value="COMPLETED">Đã điều chỉnh</option>
            <option value="REJECTED">Từ chối</option>
          </select>
        </div>
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage}
          columns={[
            { key: 'code', title: 'Mã phiếu' },
            { key: 'warehouseName', title: 'Kho' },
            { key: 'product', title: 'Sản phẩm', render: (r) => <><b>{r.sku}</b> {r.productName}</> },
            { key: 'locationCode', title: 'Vị trí' },
            { key: 'systemQuantity', title: 'Hệ thống', align: 'right' },
            { key: 'actualQuantity', title: 'Thực tế', align: 'right' },
            { key: 'difference', title: 'Chênh lệch', align: 'right', render: (r) => <b className={r.difference < 0 ? 'error' : 'ok'}>{r.difference > 0 ? '+' : ''}{r.difference}</b> },
            { key: 'reason', title: 'Lý do' },
            { key: 'createdByName', title: 'Người lập', render: (r) => <>{r.createdByName}<br /><small className="muted">{formatDateTime(r.createdAt)}</small></> },
            { key: 'status', title: 'Trạng thái', render: (r) => <StatusBadge status={r.status} /> },
            { key: 'actions', title: '', render: (r) => isManager && r.status === 'PENDING' && r.createdById !== user.id && (
              <div className="actions">
                <button className="btn btn-sm btn-success" onClick={() => setDeciding({ doc: r, action: 'approve' })}>Duyệt</button>
                <button className="btn btn-sm btn-danger" onClick={() => setDeciding({ doc: r, action: 'reject' })}>Từ chối</button>
              </div>
            ) },
          ]} />
      </div>
      <AdjustmentForm open={creating} onClose={() => setCreating(false)} onSaved={() => { setCreating(false); reload(); }} />
      <ConfirmDialog open={!!deciding} danger={deciding?.action === 'reject'} requireReason={deciding?.action === 'reject'}
        title={deciding?.action === 'approve' ? 'Duyệt điều chỉnh' : 'Từ chối điều chỉnh'}
        message={deciding && `${deciding.doc.sku}: ${deciding.doc.systemQuantity} → ${deciding.doc.actualQuantity}`}
        onCancel={() => setDeciding(null)} onConfirm={decide} />
    </>
  );
}

function AdjustmentForm({ open, onClose, onSaved }) {
  const toast = useToast();
  const [form, setForm] = useState({ warehouseId: null, product: null, rowKey: '', actualQuantity: '', reason: '' });
  const [rows, setRows] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (form.warehouseId && form.product) {
      InventoryApi.byProduct(form.warehouseId, form.product.id).then(setRows).catch(() => setRows([]));
    } else {
      setRows([]);
    }
  }, [form.warehouseId, form.product]);

  const selectedRow = rows.find((r) => String(r.id) === form.rowKey);

  const save = async () => {
    setError(null);
    try {
      await InventoryApi.createAdjustment({
        warehouseId: form.warehouseId, productId: form.product.id,
        locationId: selectedRow?.locationId ?? null, lotNumber: selectedRow?.lotNumber ?? null,
        actualQuantity: Number(form.actualQuantity), reason: form.reason,
      });
      toast.success('Đã gửi phiếu kiểm kê chờ duyệt');
      setForm({ warehouseId: form.warehouseId, product: null, rowKey: '', actualQuantity: '', reason: '' });
      onSaved();
    } catch (e) {
      setError(getErrorMessage(e));
    }
  };

  return (
    <Modal open={open} title="Phiếu kiểm kê" onClose={onClose}>
      <div className="form">
        {error && <div className="alert alert-error">{error}</div>}
        <div className="form-grid">
          <label className="field"><span>Kho</span><WarehouseSelect value={form.warehouseId} onChange={(v) => setForm({ ...form, warehouseId: v })} /></label>
          <label className="field"><span>Sản phẩm</span>
            {form.product ? <div className="row"><b>{form.product.sku}</b><button className="btn btn-sm" onClick={() => setForm({ ...form, product: null })}>Đổi</button></div>
              : <ProductPicker onSelect={(p) => setForm({ ...form, product: p })} />}
          </label>
          <label className="field field-full"><span>Vị trí / lô đang có tồn</span>
            <select value={form.rowKey} onChange={(e) => setForm({ ...form, rowKey: e.target.value })}>
              <option value="">Không vị trí / lô (hàng mới phát hiện)</option>
              {rows.map((r) => <option key={r.id} value={r.id}>{r.locationCode || '—'} · lô {r.lotNumber || '—'} · tồn {r.quantity}</option>)}
            </select>
          </label>
          <label className="field"><span>Tồn hệ thống</span><input disabled value={selectedRow?.quantity ?? 0} /></label>
          <label className="field"><span>Tồn thực tế</span><input type="number" min="0" value={form.actualQuantity} onChange={(e) => setForm({ ...form, actualQuantity: e.target.value })} /></label>
          <label className="field field-full"><span>Lý do</span><textarea rows={2} value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} /></label>
        </div>
        <div className="form-actions">
          <button className="btn" onClick={onClose}>Hủy</button>
          <button className="btn btn-primary" disabled={!form.warehouseId || !form.product || form.actualQuantity === '' || !form.reason.trim()} onClick={save}>Gửi duyệt</button>
        </div>
      </div>
    </Modal>
  );
}
