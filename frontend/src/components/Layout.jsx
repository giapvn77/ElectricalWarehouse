import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { hasPermission, logout, selectUser } from '../store/AuthStore';
import { fetchUnreadCount } from '../store/NotificationStore';
import { ROLES } from '../utils/constants';

const MENU = [
  { title: 'Tổng quan', items: [{ to: '/', label: 'Dashboard', end: true }] },
  {
    title: 'Danh mục',
    items: [
      { to: '/products', label: 'Sản phẩm' },
      { to: '/categories', label: 'Nhóm hàng & ĐVT' },
      { to: '/suppliers', label: 'Nhà cung cấp' },
      { to: '/customers', label: 'Khách hàng' },
      { to: '/warehouses', label: 'Kho & vị trí' },
    ],
  },
  {
    title: 'Nhập kho',
    items: [
      { to: '/stock-in', label: 'Phiếu nhập', end: true },
      { to: '/stock-in/new', label: 'Tạo phiếu nhập', permission: 'STOCK_IN_CREATE' },
      { to: '/stock-in/approve', label: 'Duyệt phiếu nhập', permission: 'STOCK_IN_APPROVE' },
    ],
  },
  {
    title: 'Xuất kho',
    items: [
      { to: '/stock-out', label: 'Phiếu xuất', end: true },
      { to: '/stock-out/new', label: 'Tạo phiếu xuất', permission: 'STOCK_OUT_CREATE' },
      { to: '/stock-out/approve', label: 'Duyệt phiếu xuất', permission: 'STOCK_OUT_APPROVE' },
    ],
  },
  {
    title: 'Tồn kho',
    items: [
      { to: '/inventory', label: 'Tồn kho thời gian thực', end: true },
      { to: '/inventory/adjustments', label: 'Kiểm kê / điều chỉnh' },
      { to: '/transfers', label: 'Chuyển kho' },
      { to: '/defects', label: 'Hàng lỗi' },
    ],
  },
  {
    title: 'Báo cáo',
    items: [
      { to: '/reports/inventory', label: 'Báo cáo tồn kho', permission: 'REPORT_VIEW' },
      { to: '/reports/movement', label: 'Biến động kho', permission: 'REPORT_VIEW' },
      { to: '/reports/xnt', label: 'Nhập - Xuất - Tồn', permission: 'REPORT_VIEW' },
    ],
  },
  {
    title: 'Hệ thống',
    items: [
      { to: '/users', label: 'Người dùng', permission: 'USER_MANAGE' },
      { to: '/roles', label: 'Phân quyền', permission: 'USER_MANAGE' },
    ],
  },
];

export default function Layout() {
  const user = useSelector(selectUser);
  const unread = useSelector((s) => s.notifications.unreadCount);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    dispatch(fetchUnreadCount());
    const timer = setInterval(() => dispatch(fetchUnreadCount()), 60_000);
    return () => clearInterval(timer);
  }, [dispatch]);

  const handleLogout = async () => {
    await dispatch(logout());
    navigate('/login');
  };

  return (
    <div className={`layout ${menuOpen ? 'menu-open' : ''}`}>
      <aside className="sidebar" onClick={() => setMenuOpen(false)}>
        <div className="brand">⚡ Kho Thiết Bị Điện</div>
        {MENU.map((group) => {
          const items = group.items.filter((i) => hasPermission(user, i.permission));
          if (items.length === 0) return null;
          return (
            <nav key={group.title}>
              <div className="nav-title">{group.title}</div>
              {items.map((i) => (
                <NavLink key={i.to} to={i.to} end={i.end} className="nav-link">{i.label}</NavLink>
              ))}
            </nav>
          );
        })}
      </aside>
      <div className="main">
        <header className="topbar">
          <button className="btn btn-ghost menu-toggle" onClick={() => setMenuOpen((v) => !v)} aria-label="Menu">☰</button>
          <div className="grow" />
          <Link to="/notifications" className="bell" title="Thông báo">
            🔔{unread > 0 && <span className="bell-count">{unread > 99 ? '99+' : unread}</span>}
          </Link>
          <div className="user">
            <b>{user?.fullName}</b>
            <small className="muted">{user?.roles?.map((r) => ROLES[r] || r).join(', ')}</small>
          </div>
          <button className="btn btn-sm" onClick={handleLogout}>Đăng xuất</button>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
