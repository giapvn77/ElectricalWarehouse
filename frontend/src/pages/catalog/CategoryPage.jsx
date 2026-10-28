import { useState } from 'react';
import { ProductApi } from '../../api/ProductApi';
import BaseForm from '../../components/BaseForm';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { useToast } from '../../hooks/useToast';

/** Product categories and units of measure. */
export default function CategoryPage() {
  const toast = useToast();
  const categories = useAsync(() => ProductApi.categories(), []);
  const units = useAsync(() => ProductApi.units(), []);
  const [editing, setEditing] = useState(null); // { kind: 'category' | 'unit', value }

  const categoryFields = [
    { name: 'code', label: 'Mã nhóm', required: true },
    { name: 'name', label: 'Tên nhóm', required: true },
    { name: 'parentId', label: 'Nhóm cha', type: 'select', numeric: true,
      options: (categories.data || []).filter((c) => c.id !== editing?.value?.id).map((c) => ({ value: c.id, label: c.name })) },
    { name: 'active', label: 'Đang sử dụng', type: 'checkbox' },
  ];
  const unitFields = [
    { name: 'code', label: 'Mã ĐVT', required: true },
    { name: 'name', label: 'Tên ĐVT', required: true },
  ];

  const save = async (values) => {
    if (editing.kind === 'category') {
      await ProductApi.saveCategory(editing.value.id, values);
      categories.reload();
    } else {
      await ProductApi.saveUnit(editing.value.id, values);
      units.reload();
    }
    toast.success('Đã lưu');
    setEditing(null);
  };

  return (
    <>
      <PageHeader title="Nhóm hàng & đơn vị tính" />
      <div className="grid-2">
        <div className="card">
          <div className="card-header">
            <h2>Nhóm hàng</h2>
            <button className="btn btn-primary btn-sm" onClick={() => setEditing({ kind: 'category', value: { active: true } })}>+ Thêm</button>
          </div>
          <DataTable loading={categories.loading} rows={categories.data || []} onRowClick={(c) => setEditing({ kind: 'category', value: c })}
            columns={[
              { key: 'code', title: 'Mã' },
              { key: 'name', title: 'Tên' },
              { key: 'parentName', title: 'Nhóm cha' },
            ]} />
        </div>
        <div className="card">
          <div className="card-header">
            <h2>Đơn vị tính</h2>
            <button className="btn btn-primary btn-sm" onClick={() => setEditing({ kind: 'unit', value: {} })}>+ Thêm</button>
          </div>
          <DataTable loading={units.loading} rows={units.data || []} onRowClick={(u) => setEditing({ kind: 'unit', value: u })}
            columns={[{ key: 'code', title: 'Mã' }, { key: 'name', title: 'Tên' }]} />
        </div>
      </div>
      <Modal open={!!editing} title={editing?.kind === 'unit' ? 'Đơn vị tính' : 'Nhóm hàng'} onClose={() => setEditing(null)} width={520}>
        {editing && <BaseForm fields={editing.kind === 'unit' ? unitFields : categoryFields} initialValues={editing.value} onSubmit={save} onCancel={() => setEditing(null)} />}
      </Modal>
    </>
  );
}
