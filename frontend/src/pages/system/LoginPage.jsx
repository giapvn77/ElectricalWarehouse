import { useState } from 'react';
import { useDispatch } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import { login } from '../../store/AuthStore';

export default function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const result = await dispatch(login(form));
    setBusy(false);
    if (login.fulfilled.match(result)) navigate(location.state?.from?.pathname || '/', { replace: true });
    else setError('Sai tên đăng nhập/mật khẩu hoặc tài khoản đã bị khóa');
  };

  return (
    <div className="login-page">
      <form className="card login-card" onSubmit={submit}>
        <h1>⚡ Kho Thiết Bị Điện</h1>
        <p className="muted">Đăng nhập để tiếp tục</p>
        {error && <div className="alert alert-error">{error}</div>}
        <label className="field">
          <span>Tên đăng nhập</span>
          <input autoFocus required value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
        </label>
        <label className="field">
          <span>Mật khẩu</span>
          <input type="password" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </label>
        <button className="btn btn-primary w-full" disabled={busy}>{busy ? 'Đang đăng nhập...' : 'Đăng nhập'}</button>
      </form>
    </div>
  );
}
