import { useCallback, useState } from 'react';
import BaseForm from '../../components/BaseForm';
import ConfirmDialog from '../../components/ConfirmDialog';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { useToast } from '../../hooks/useToast';

const FIELDS = [
  { name: 'code', label: 'Mã', required: true },
  { name: 'name', label: 'Tên', required: true },
  { name: 'taxCode', label: 'Mã số thuế' },
  { name: 'phone', label: 'Điện thoại' },
  { name: 'email', label: 'Email', type: 'email' },
  { name: 'contactPerson', label: 'Người liên hệ' },
  { name: 'address', label: 'Địa chỉ', full: true },
  { name: 'active', label: 'Đang giao dịch', type: 'checkbox' },
];

/** Shared list + form for suppliers and customers (same fields). */
export default function PartnerManager({ title, api }) {
  const toast = useToast();
  const [keyword, setKeyword] = useState('');
  const [filters, setFilters] = useState({});
  const [editing, setEditing] = useState(null);
  const [removing, setRemoving] = useState(null);
  const fetcher = useCallback((p) => api.search(p), [api]);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters);

  const save = async (values) => {
    if (editing.id) await api.update(editing.id, values);
    else await api.create(values);
    toast.success('Đã lưu');
    setEditing(null);
    reload();
  };

  const deactivate = async () => {
    try {
      await api.deactivate(removing.id);
      toast.success('Đã ngừng giao dịch');
      setRemoving(null);
      reload();
    } catch (e) {
      toast.error(e);
    }
  };

  return (
    <>
      <PageHeader title={title} actions={<button className="btn btn-primary" onClick={() => setEditing({ active: true })}>+ Thêm mới</button>} />
      <div className="card">
        <form className="filters" onSubmit={(e) => { e.preventDefault(); setFilters({ keyword }); }}>
          <input placeholder="Mã, tên, số điện thoại" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <button className="btn">Tìm</button>
        </form>
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage}
          columns={[
            { key: 'code', title: 'Mã' },
            { key: 'name', title: 'Tên' },
            { key: 'phone', title: 'Điện thoại' },
            { key: 'email', title: 'Email' },
            { key: 'contactPerson', title: 'Liên hệ' },
            { key: 'active', title: 'Trạng thái', render: (r) => <span className={`badge badge-${r.active ? 'green' : 'gray'}`}>{r.active ? 'Đang GD' : 'Ngừng GD'}</span> },
            { key: 'actions', title: '', align: 'right', render: (r) => (
              <div className="actions">
                <button className="btn btn-sm" onClick={() => setEditing(r)}>Sửa</button>
                {r.active && <button className="btn btn-sm" onClick={() => setRemoving(r)}>Ngừng</button>}
              </div>
            ) },
          ]} />
      </div>
      <Modal open={!!editing} title={editing?.id ? 'Cập nhật' : 'Thêm mới'} onClose={() => setEditing(null)}>
        {editing && <BaseForm fields={FIELDS} initialValues={editing} onSubmit={save} onCancel={() => setEditing(null)} />}
      </Modal>
      <ConfirmDialog open={!!removing} danger message={`Ngừng giao dịch với ${removing?.name}?`} onCancel={() => setRemoving(null)} onConfirm={deactivate} />
    </>
  );
}
