import { useState } from 'react';
import { UserApi } from '../../api/UserApi';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { useToast } from '../../hooks/useToast';
import { ROLES } from '../../utils/constants';

export default function RolePermissionPage() {
  const toast = useToast();
  const roles = useAsync(() => UserApi.roles(), []);
  const permissions = useAsync(() => UserApi.permissions(), []);
  const [draft, setDraft] = useState({});

  const selected = (role) => draft[role.id] ?? role.permissions;

  const toggle = (role, code) => {
    const current = selected(role);
    setDraft({ ...draft, [role.id]: current.includes(code) ? current.filter((c) => c !== code) : [...current, code] });
  };

  const save = async (role) => {
    try {
      await UserApi.updateRolePermissions(role.id, selected(role));
      toast.success('Đã cập nhật quyền cho ' + (ROLES[role.code] || role.code));
      setDraft({ ...draft, [role.id]: undefined });
      roles.reload();
    } catch (e) {
      toast.error(e);
    }
  };

  if (roles.loading || permissions.loading) return <p className="muted">Đang tải...</p>;

  return (
    <>
      <PageHeader title="Phân quyền" subtitle="ADMIN luôn có toàn quyền. Người lập phiếu không bao giờ được tự duyệt phiếu của mình." />
      <div className="card table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th>Quyền</th>
              {roles.data.map((r) => <th key={r.id} className="center">{ROLES[r.code] || r.code}</th>)}
            </tr>
          </thead>
          <tbody>
            {permissions.data.map((p) => (
              <tr key={p.code}>
                <td><code>{p.code}</code></td>
                {roles.data.map((r) => (
                  <td key={r.id} className="center">
                    <input type="checkbox" disabled={r.code === 'ADMIN'} checked={selected(r).includes(p.code)} onChange={() => toggle(r, p.code)} />
                  </td>
                ))}
              </tr>
            ))}
            <tr>
              <td />
              {roles.data.map((r) => (
                <td key={r.id} className="center">
                  {r.code !== 'ADMIN' && <button className="btn btn-sm btn-primary" disabled={!draft[r.id]} onClick={() => save(r)}>Lưu</button>}
                </td>
              ))}
            </tr>
          </tbody>
        </table>
      </div>
    </>
  );
}
