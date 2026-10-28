import { useState } from 'react';
import { InventoryApi } from '../../api/InventoryApi';
import BaseForm from '../../components/BaseForm';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import ProductPicker from '../../components/ProductPicker';
import { useAsync } from '../../hooks/useAsync';
import { useToast } from '../../hooks/useToast';
import { invalidateWarehouses } from '../../hooks/useWarehouses';
import { WAREHOUSE_TYPE, toOptions } from '../../utils/constants';

const WAREHOUSE_FIELDS = [
  { name: 'code', label: 'Mã kho', required: true },
  { name: 'name', label: 'Tên kho', required: true },
  { name: 'type', label: 'Loại kho', type: 'select', required: true, options: toOptions(WAREHOUSE_TYPE) },
  { name: 'managerEmail', label: 'Email nhận thông báo duyệt', type: 'email' },
  { name: 'address', label: 'Địa chỉ', full: true },
  { name: 'active', label: 'Đang hoạt động', type: 'checkbox' },
];
const LOCATION_FIELDS = [
  { name: 'code', label: 'Mã vị trí (VD: A-01-02)', required: true },
  { name: 'zone', label: 'Khu' },
  { name: 'description', label: 'Mô tả', full: true },
  { name: 'active', label: 'Đang sử dụng', type: 'checkbox' },
];

/** Warehouses, their storage locations and Min-Max thresholds. */
export default function WarehousePage() {
  const toast = useToast();
  const warehouses = useAsync(() => InventoryApi.warehouses(false), []);
  const [selected, setSelected] = useState(null);
  const locations = useAsync(() => (selected ? InventoryApi.locations(selected.id) : Promise.resolve([])), [selected?.id]);
  const thresholds = useAsync(() => (selected ? InventoryApi.thresholds(selected.id) : Promise.resolve([])), [selected?.id]);
  const [editing, setEditing] = useState(null); // { kind, value }
  const [threshold, setThreshold] = useState(null);

  const save = async (values) => {
    if (editing.kind === 'warehouse') {
      await InventoryApi.saveWarehouse(editing.value.id, values);
      invalidateWarehouses();
      warehouses.reload();
    } else {
      await InventoryApi.saveLocation(selected.id, editing.value.id, values);
      locations.reload();
    }
    toast.success('Đã lưu');
    setEditing(null);
  };

  const saveThreshold = async () => {
    try {
      await InventoryApi.saveThreshold({ warehouseId: selected.id, productId: threshold.product.id,
        minQuantity: Number(threshold.min), maxQuantity: Number(threshold.max) });
      toast.success('Đã lưu định mức');
      setThreshold(null);
      thresholds.reload();
    } catch (e) {
      toast.error(e);
    }
  };

  const removeThreshold = async (t) => {
    await InventoryApi.deleteThreshold(t.id);
    thresholds.reload();
  };

  return (
    <>
      <PageHeader title="Kho & vị trí" actions={<button className="btn btn-primary" onClick={() => setEditing({ kind: 'warehouse', value: { type: 'MAIN', active: true } })}>+ Thêm kho</button>} />
      <div className="card">
        <DataTable loading={warehouses.loading} rows={warehouses.data || []} onRowClick={setSelected}
          columns={[
            { key: 'code', title: 'Mã' },
            { key: 'name', title: 'Tên kho' },
            { key: 'type', title: 'Loại', render: (w) => WAREHOUSE_TYPE[w.type] },
            { key: 'address', title: 'Địa chỉ' },
            { key: 'active', title: 'Trạng thái', render: (w) => (w.active ? 'Hoạt động' : 'Ngừng') },
            { key: 'actions', title: '', align: 'right', render: (w) => (
              <button className="btn btn-sm" onClick={(e) => { e.stopPropagation(); setEditing({ kind: 'warehouse', value: w }); }}>Sửa</button>
            ) },
          ]} />
      </div>

      {selected && (
        <div className="grid-2">
          <div className="card">
            <div className="card-header">
              <h2>Vị trí trong {selected.name}</h2>
              <button className="btn btn-sm btn-primary" onClick={() => setEditing({ kind: 'location', value: { active: true } })}>+ Vị trí</button>
            </div>
            <DataTable loading={locations.loading} rows={locations.data || []} onRowClick={(l) => setEditing({ kind: 'location', value: l })}
              columns={[{ key: 'code', title: 'Mã' }, { key: 'zone', title: 'Khu' }, { key: 'description', title: 'Mô tả' }]} />
          </div>
          <div className="card">
            <div className="card-header">
              <h2>Định mức tồn Min - Max</h2>
              <button className="btn btn-sm btn-primary" onClick={() => setThreshold({ product: null, min: 0, max: 0 })}>+ Định mức</button>
            </div>
            <DataTable loading={thresholds.loading} rows={thresholds.data || []}
              columns={[
                { key: 'sku', title: 'Mã hàng' },
                { key: 'productName', title: 'Tên hàng' },
                { key: 'minQuantity', title: 'Min', align: 'right' },
                { key: 'maxQuantity', title: 'Max', align: 'right' },
                { key: 'x', title: '', render: (t) => <button className="btn btn-sm" onClick={() => removeThreshold(t)}>Xóa</button> },
              ]} />
          </div>
        </div>
      )}

      <Modal open={!!editing} title={editing?.kind === 'location' ? 'Vị trí lưu trữ' : 'Kho'} onClose={() => setEditing(null)}>
        {editing && <BaseForm fields={editing.kind === 'location' ? LOCATION_FIELDS : WAREHOUSE_FIELDS} initialValues={editing.value} onSubmit={save} onCancel={() => setEditing(null)} />}
      </Modal>

      <Modal open={!!threshold} title="Định mức tồn kho" onClose={() => setThreshold(null)} width={520}>
        {threshold && (
          <div className="form">
            {threshold.product ? <p><b>{threshold.product.sku}</b> {threshold.product.name}</p>
              : <ProductPicker onSelect={(p) => setThreshold({ ...threshold, product: p })} />}
            <div className="form-grid">
              <label className="field"><span>Tối thiểu</span><input type="number" min="0" value={threshold.min} onChange={(e) => setThreshold({ ...threshold, min: e.target.value })} /></label>
              <label className="field"><span>Tối đa</span><input type="number" min="0" value={threshold.max} onChange={(e) => setThreshold({ ...threshold, max: e.target.value })} /></label>
            </div>
            <div className="form-actions">
              <button className="btn" onClick={() => setThreshold(null)}>Hủy</button>
              <button className="btn btn-primary" disabled={!threshold.product} onClick={saveThreshold}>Lưu</button>
            </div>
          </div>
        )}
      </Modal>
    </>
  );
}
