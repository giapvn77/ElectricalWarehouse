import { useCallback, useState } from 'react';
import { useSelector } from 'react-redux';
import { TransferApi } from '../../api/TransferApi';
import BaseForm from '../../components/BaseForm';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import ProductPicker from '../../components/ProductPicker';
import StatusBadge from '../../components/StatusBadge';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { useToast } from '../../hooks/useToast';
import { useWarehouses } from '../../hooks/useWarehouses';
import { hasPermission, hasRole, selectUser } from '../../store/AuthStore';
import { DEFECT_SOURCE, DEFECT_STATUS, toOptions } from '../../utils/constants';
import { formatDateTime } from '../../utils/format';

const RESOLUTIONS = [
  { value: 'REPAIRED', label: 'Đã sửa - trả về kho gốc' },
  { value: 'RETURNED_TO_SUPPLIER', label: 'Trả nhà cung cấp' },
  { value: 'DISPOSED', label: 'Tiêu hủy' },
];

export default function DefectiveGoodsPage() {
  const user = useSelector(selectUser);
  const toast = useToast();
  const warehouses = useWarehouses().filter((w) => w.type !== 'DEFECTIVE');
  const [filters, setFilters] = useState({});
  const fetcher = useCallback((p) => TransferApi.defects(p), []);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters);
  const [product, setProduct] = useState(null);
  const [registering, setRegistering] = useState(false);
  const [resolving, setResolving] = useState(null);

  const register = async (values) => {
    await TransferApi.registerDefect({ ...values, productId: product.id, quantity: values.quantity ?? 1 });
    toast.success('Đã ghi nhận hàng lỗi');
    setRegistering(false);
    setProduct(null);
    reload();
  };

  const resolve = async (values) => {
    await TransferApi.resolveDefect(resolving.id, values.status, values.note);
    toast.success('Đã xử lý hàng lỗi');
    setResolving(null);
    reload();
  };

  return (
    <>
      <PageHeader title="Hàng lỗi" subtitle="Hàng lỗi được chuyển vào kho loại DEFECTIVE cho tới khi sửa, trả NCC hoặc tiêu hủy."
        actions={hasPermission(user, 'DEFECT_MANAGE') && <button className="btn btn-primary" onClick={() => setRegistering(true)}>+ Ghi nhận hàng lỗi</button>} />
      <div className="card">
        <div className="filters">
          <select value={filters.status || ''} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
            <option value="">Tất cả trạng thái</option>
            {toOptions(DEFECT_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage}
          columns={[
            { key: 'code', title: 'Mã' },
            { key: 'product', title: 'Sản phẩm', render: (r) => <><b>{r.sku}</b> {r.productName}</> },
            { key: 'serialNumber', title: 'Serial' },
            { key: 'quantity', title: 'SL', align: 'right' },
            { key: 'source', title: 'Nguồn', render: (r) => DEFECT_SOURCE[r.source] },
            { key: 'sourceWarehouseName', title: 'Kho phát sinh' },
            { key: 'description', title: 'Mô tả lỗi' },
            { key: 'reportedByName', title: 'Người ghi nhận', render: (r) => <>{r.reportedByName}<br /><small className="muted">{formatDateTime(r.createdAt)}</small></> },
            { key: 'status', title: 'Trạng thái', render: (r) => <StatusBadge status={r.status} map={DEFECT_STATUS} /> },
            { key: 'x', title: '', render: (r) => r.status === 'IN_DEFECTIVE_WAREHOUSE' && hasRole(user, 'ADMIN', 'QLK') && (
              <button className="btn btn-sm" onClick={() => setResolving(r)}>Xử lý</button>
            ) },
          ]} />
      </div>

      <Modal open={registering} title="Ghi nhận hàng lỗi" onClose={() => { setRegistering(false); setProduct(null); }}>
        {product ? (
          <>
            <p><b>{product.sku}</b> {product.name} <button className="btn btn-sm" onClick={() => setProduct(null)}>Đổi</button></p>
            <BaseForm onSubmit={register} onCancel={() => setRegistering(false)} submitLabel="Ghi nhận"
              initialValues={{ quantity: 1, source: 'STORAGE' }}
              fields={[
                { name: 'warehouseId', label: 'Kho phát sinh', type: 'select', numeric: true, required: true, options: warehouses.map((w) => ({ value: w.id, label: w.name })) },
                { name: 'source', label: 'Nguồn', type: 'select', required: true, options: toOptions(DEFECT_SOURCE) },
                ...(product.serialManaged ? [{ name: 'serialNumber', label: 'Số serial', required: true }] : [{ name: 'quantity', label: 'Số lượng', type: 'number', required: true }]),
                { name: 'description', label: 'Mô tả lỗi', type: 'textarea', required: true },
              ]} />
          </>
        ) : <ProductPicker onSelect={setProduct} />}
      </Modal>

      <Modal open={!!resolving} title={`Xử lý hàng lỗi ${resolving?.code || ''}`} onClose={() => setResolving(null)} width={520}>
        {resolving && (
          <BaseForm onSubmit={resolve} onCancel={() => setResolving(null)} submitLabel="Xác nhận"
            fields={[
              { name: 'status', label: 'Hướng xử lý', type: 'select', required: true, options: RESOLUTIONS },
              { name: 'note', label: 'Ghi chú', type: 'textarea' },
            ]} />
        )}
      </Modal>
    </>
  );
}
