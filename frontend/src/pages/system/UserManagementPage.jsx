import { useCallback, useMemo, useState } from 'react';
import { UserApi } from '../../api/UserApi';
import BaseForm from '../../components/BaseForm';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { useToast } from '../../hooks/useToast';
import { useWarehouses } from '../../hooks/useWarehouses';
import { ROLES, toOptions } from '../../utils/constants';

export default function UserManagementPage() {
  const toast = useToast();
  const warehouses = useWarehouses();
  const [keyword, setKeyword] = useState('');
  const [filters, setFilters] = useState({});
  const [editing, setEditing] = useState(null);
  const fetcher = useCallback((p) => UserApi.search(p), []);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters);

  const fields = useMemo(() => [
    { name: 'username', label: 'Tên đăng nhập', required: true },
    { name: 'password', label: editing?.id ? 'Mật khẩu mới (bỏ trống nếu giữ nguyên)' : 'Mật khẩu', type: 'password', required: !editing?.id },
    { name: 'fullName', label: 'Họ tên', required: true },
    { name: 'email', label: 'Email', type: 'email' },
    { name: 'phone', label: 'Điện thoại' },
    { name: 'warehouseId', label: 'Kho trực thuộc', type: 'select', numeric: true, placeholder: 'Toàn hệ thống',
      options: warehouses.map((w) => ({ value: w.id, label: w.name })) },
    { name: 'roles', label: 'Vai trò', type: 'multiselect', full: true, options: toOptions(ROLES) },
  ], [editing, warehouses]);

  const save = async (values) => {
    const body = { ...values, password: values.password || null };
    if (editing.id) await UserApi.update(editing.id, body);
    else await UserApi.create(body);
    toast.success('Đã lưu người dùng');
    setEditing(null);
    reload();
  };

  const toggle = async (u) => {
    try {
      await UserApi.setActive(u.id, !u.active);
      reload();
    } catch (e) {
      toast.error(e);
    }
  };

  return (
    <>
      <PageHeader title="Người dùng" actions={<button className="btn btn-primary" onClick={() => setEditing({ roles: [] })}>+ Thêm người dùng</button>} />
      <div className="card">
        <form className="filters" onSubmit={(e) => { e.preventDefault(); setFilters({ keyword }); }}>
          <input placeholder="Tìm theo tên đăng nhập, họ tên" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <button className="btn">Tìm</button>
        </form>
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage}
          columns={[
            { key: 'username', title: 'Tên đăng nhập' },
            { key: 'fullName', title: 'Họ tên' },
            { key: 'roles', title: 'Vai trò', render: (u) => u.roles.map((r) => ROLES[r] || r).join(', ') },
            { key: 'warehouseName', title: 'Kho', render: (u) => u.warehouseName || 'Toàn hệ thống' },
            { key: 'active', title: 'Trạng thái', render: (u) => <span className={`badge badge-${u.active ? 'green' : 'gray'}`}>{u.active ? 'Hoạt động' : 'Đã khóa'}</span> },
            { key: 'actions', title: '', align: 'right', render: (u) => (
              <div className="actions">
                <button className="btn btn-sm" onClick={() => setEditing({ ...u, password: '' })}>Sửa</button>
                <button className="btn btn-sm" onClick={() => toggle(u)}>{u.active ? 'Khóa' : 'Mở khóa'}</button>
              </div>
            ) },
          ]} />
      </div>
      <Modal open={!!editing} title={editing?.id ? 'Sửa người dùng' : 'Thêm người dùng'} onClose={() => setEditing(null)}>
        {editing && <BaseForm fields={fields} initialValues={editing} onSubmit={save} onCancel={() => setEditing(null)} />}
      </Modal>
    </>
  );
}
