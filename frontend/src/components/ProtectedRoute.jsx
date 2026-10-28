import { useSelector } from 'react-redux';
import { Navigate, useLocation } from 'react-router-dom';
import { hasPermission, selectAuthStatus, selectUser } from '../store/AuthStore';

export default function ProtectedRoute({ permission, roles, children }) {
  const status = useSelector(selectAuthStatus);
  const user = useSelector(selectUser);
  const location = useLocation();

  if (status === 'idle' || status === 'loading') return <div className="center muted p-4">Đang tải...</div>;
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  const roleOk = !roles || roles.some((r) => user.roles.includes(r)) || user.roles.includes('ADMIN');
  if (!hasPermission(user, permission) || !roleOk) {
    return <div className="card"><h2>Không có quyền truy cập</h2><p className="muted">Liên hệ quản trị viên để được cấp quyền.</p></div>;
  }
  return children;
}
