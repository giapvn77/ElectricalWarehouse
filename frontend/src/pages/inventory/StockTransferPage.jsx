import { useCallback, useState } from 'react';
import { useSelector } from 'react-redux';
import { TransferApi } from '../../api/TransferApi';
import DataTable from '../../components/DataTable';
import DocumentActions from '../../components/DocumentActions';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import ProductPicker from '../../components/ProductPicker';
import StatusBadge from '../../components/StatusBadge';
import WarehouseSelect from '../../components/WarehouseSelect';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { useToast } from '../../hooks/useToast';
import { hasPermission, selectUser } from '../../store/AuthStore';
import { formatDateTime, getErrorMessage } from '../../utils/format';
import DocumentHeader from '../shared/DocumentHeader';

export default function StockTransferPage() {
  const user = useSelector(selectUser);
  const toast = useToast();
  const [filters, setFilters] = useState({});
  const fetcher = useCallback((p) => TransferApi.search(p), []);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters);
  const [creating, setCreating] = useState(false);
  const [viewing, setViewing] = useState(null);

  const receive = async () => {
    try {
      setViewing(await TransferApi.receive(viewing.id));
      toast.success('Đã xác nhận nhận hàng');
      reload();
    } catch (e) {
      toast.error(e);
    }
  };

  return (
    <>
      <PageHeader title="Chuyển kho"
        actions={hasPermission(user, 'TRANSFER_MANAGE') && <button className="btn btn-primary" onClick={() => setCreating(true)}>+ Phiếu chuyển kho</button>} />
      <div className="card">
        <div className="filters">
          <WarehouseSelect allowAll value={filters.warehouseId} onChange={(v) => setFilters({ ...filters, warehouseId: v })} />
        </div>
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage} onRowClick={setViewing}
          columns={[
            { key: 'code', title: 'Mã phiếu' },
            { key: 'fromWarehouseName', title: 'Từ kho' },
            { key: 'toWarehouseName', title: 'Đến kho' },
            { key: 'items', title: 'Số dòng', align: 'right', render: (r) => r.details.length },
            { key: 'createdByName', title: 'Người lập' },
            { key: 'createdAt', title: 'Ngày lập', render: (r) => formatDateTime(r.createdAt) },
            { key: 'status', title: 'Trạng thái', render: (r) => <StatusBadge status={r.status === 'APPROVED' ? 'APPROVED' : r.status} /> },
          ]} />
      </div>

      <TransferForm open={creating} onClose={() => setCreating(false)} onSaved={(t) => { setCreating(false); reload(); setViewing(t); }} />

      <Modal open={!!viewing} title={`Phiếu chuyển kho ${viewing?.code || ''}`} onClose={() => setViewing(null)} width={820}>
        {viewing && (
          <>
            <p><b>{viewing.fromWarehouseName}</b> → <b>{viewing.toWarehouseName}</b></p>
            <DocumentHeader doc={viewing}>
              {viewing.status === 'APPROVED' && <div className="alert">Hàng đang trên đường, chờ kho đích xác nhận.</div>}
              {viewing.receivedByName && <div><span className="muted">Người nhận</span>{viewing.receivedByName} · {formatDateTime(viewing.receivedAt)}</div>}
            </DocumentHeader>
            <DataTable rows={viewing.details}
              columns={[
                { key: 'sku', title: 'Mã hàng' },
                { key: 'productName', title: 'Tên hàng' },
                { key: 'quantity', title: 'SL', align: 'right' },
                { key: 'toLocationCode', title: 'Vị trí đích' },
                { key: 'serialNumbers', title: 'Serial' },
              ]} />
            <DocumentActions api={TransferApi} doc={viewing} approveRoles={['ADMIN', 'QLK']}
              onChanged={(t) => { setViewing(t); reload(); }}
              extra={viewing.status === 'APPROVED' && hasPermission(user, 'TRANSFER_MANAGE') && (
                <button className="btn btn-success" onClick={receive}>Xác nhận đã nhận hàng</button>
              )} />
          </>
        )}
      </Modal>
    </>
  );
}

function TransferForm({ open, onClose, onSaved }) {
  const [form, setForm] = useState({ fromWarehouseId: null, toWarehouseId: null, note: '' });
  const [items, setItems] = useState([]);
  const [error, setError] = useState(null);

  const add = (p) => setItems([...items, { productId: p.id, sku: p.sku, name: p.name, serialManaged: p.serialManaged, quantity: 1, serials: '' }]);
  const update = (idx, changes) => setItems(items.map((it, i) => (i === idx ? { ...it, ...changes } : it)));

  const save = async () => {
    setError(null);
    try {
      const t = await TransferApi.create({
        ...form,
        items: items.map((i) => ({
          productId: i.productId, quantity: Number(i.quantity),
          serialNumbers: i.serialManaged ? i.serials.split(/[\s,;]+/).filter(Boolean) : null,
        })),
      });
      setItems([]);
      onSaved(t);
    } catch (e) {
      setError(getErrorMessage(e));
    }
  };

  return (
    <Modal open={open} title="Tạo phiếu chuyển kho" onClose={onClose} width={820}>
      <div className="form">
        {error && <div className="alert alert-error">{error}</div>}
        <div className="form-grid">
          <label className="field"><span>Từ kho</span><WarehouseSelect value={form.fromWarehouseId} onChange={(v) => setForm({ ...form, fromWarehouseId: v })} /></label>
          <label className="field"><span>Đến kho</span><WarehouseSelect value={form.toWarehouseId} exclude={form.fromWarehouseId} onChange={(v) => setForm({ ...form, toWarehouseId: v })} /></label>
          <label className="field field-full"><span>Ghi chú</span><input value={form.note} onChange={(e) => setForm({ ...form, note: e.target.value })} /></label>
        </div>
        <ProductPicker onSelect={add} />
        <table className="table">
          <thead><tr><th>Sản phẩm</th><th>Số lượng</th><th>Serial (cách nhau bởi dấu phẩy)</th><th /></tr></thead>
          <tbody>
            {items.map((i, idx) => (
              <tr key={idx}>
                <td><b>{i.sku}</b> {i.name}</td>
                <td><input type="number" min="1" className="w-sm" value={i.quantity} onChange={(e) => update(idx, { quantity: e.target.value })} /></td>
                <td>{i.serialManaged ? <input className="w-full" value={i.serials} onChange={(e) => update(idx, { serials: e.target.value })} /> : '—'}</td>
                <td><button className="btn btn-sm btn-ghost" onClick={() => setItems(items.filter((_, j) => j !== idx))}>✕</button></td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="form-actions">
          <button className="btn" onClick={onClose}>Hủy</button>
          <button className="btn btn-primary" disabled={!form.fromWarehouseId || !form.toWarehouseId || items.length === 0} onClick={save}>Lưu nháp</button>
        </div>
      </div>
    </Modal>
  );
}
