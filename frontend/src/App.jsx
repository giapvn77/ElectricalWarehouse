import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { setSessionExpiredHandler } from './api/httpClient';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Toast from './components/Toast';
import { loadCurrentUser, selectAuthStatus, sessionExpired } from './store/AuthStore';

import CategoryPage from './pages/catalog/CategoryPage';
import CustomerPage from './pages/catalog/CustomerPage';
import ProductCreatePage from './pages/catalog/ProductCreatePage';
import ProductListPage from './pages/catalog/ProductListPage';
import SupplierPage from './pages/catalog/SupplierPage';
import WarehousePage from './pages/catalog/WarehousePage';
import DefectiveGoodsPage from './pages/inventory/DefectiveGoodsPage';
import InventoryAdjustmentPage from './pages/inventory/InventoryAdjustmentPage';
import InventoryRealTimePage from './pages/inventory/InventoryRealTimePage';
import StockTransferPage from './pages/inventory/StockTransferPage';
import InventoryReportPage from './pages/reports/InventoryReportPage';
import MovementReportPage from './pages/reports/MovementReportPage';
import XNTReportPage from './pages/reports/XNTReportPage';
import StockInApprovePage from './pages/stockin/StockInApprovePage';
import StockInCreatePage from './pages/stockin/StockInCreatePage';
import StockInDetailPage from './pages/stockin/StockInDetailPage';
import StockInListPage from './pages/stockin/StockInListPage';
import StockInScanPage from './pages/stockin/StockInScanPage';
import StockOutApprovePage from './pages/stockout/StockOutApprovePage';
import StockOutCreatePage from './pages/stockout/StockOutCreatePage';
import StockOutDetailPage from './pages/stockout/StockOutDetailPage';
import StockOutListPage from './pages/stockout/StockOutListPage';
import StockOutPickPage from './pages/stockout/StockOutPickPage';
import DashboardPage from './pages/system/DashboardPage';
import LoginPage from './pages/system/LoginPage';
import NotificationsPage from './pages/system/NotificationsPage';
import RolePermissionPage from './pages/system/RolePermissionPage';
import UserManagementPage from './pages/system/UserManagementPage';

const guard = (element, permission, roles) => <ProtectedRoute permission={permission} roles={roles}>{element}</ProtectedRoute>;

export default function App() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const status = useSelector(selectAuthStatus);

  useEffect(() => {
    setSessionExpiredHandler(() => {
      dispatch(sessionExpired());
      navigate('/login');
    });
  }, [dispatch, navigate]);

  useEffect(() => {
    if (status === 'idle') dispatch(loadCurrentUser());
  }, [status, dispatch]);

  return (
    <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={guard(<Layout />)}>
          <Route index element={<DashboardPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route path="users" element={guard(<UserManagementPage />, 'USER_MANAGE')} />
          <Route path="roles" element={guard(<RolePermissionPage />, 'USER_MANAGE')} />

          <Route path="products" element={<ProductListPage />} />
          <Route path="products/new" element={guard(<ProductCreatePage />, 'CATALOG_MANAGE')} />
          <Route path="products/:id" element={<ProductCreatePage />} />
          <Route path="categories" element={<CategoryPage />} />
          <Route path="suppliers" element={<SupplierPage />} />
          <Route path="customers" element={<CustomerPage />} />
          <Route path="warehouses" element={<WarehousePage />} />

          <Route path="stock-in" element={<StockInListPage />} />
          <Route path="stock-in/new" element={guard(<StockInCreatePage />, 'STOCK_IN_CREATE')} />
          <Route path="stock-in/approve" element={guard(<StockInApprovePage />, 'STOCK_IN_APPROVE')} />
          <Route path="stock-in/:id" element={<StockInDetailPage />} />
          <Route path="stock-in/:id/scan" element={guard(<StockInScanPage />, 'STOCK_IN_CREATE')} />

          <Route path="stock-out" element={<StockOutListPage />} />
          <Route path="stock-out/new" element={guard(<StockOutCreatePage />, 'STOCK_OUT_CREATE')} />
          <Route path="stock-out/approve" element={guard(<StockOutApprovePage />, 'STOCK_OUT_APPROVE')} />
          <Route path="stock-out/:id" element={<StockOutDetailPage />} />
          <Route path="stock-out/:id/pick" element={guard(<StockOutPickPage />, 'STOCK_OUT_PICK')} />

          <Route path="inventory" element={<InventoryRealTimePage />} />
          <Route path="inventory/adjustments" element={<InventoryAdjustmentPage />} />
          <Route path="transfers" element={<StockTransferPage />} />
          <Route path="defects" element={<DefectiveGoodsPage />} />

          <Route path="reports/inventory" element={guard(<InventoryReportPage />, 'REPORT_VIEW')} />
          <Route path="reports/movement" element={guard(<MovementReportPage />, 'REPORT_VIEW')} />
          <Route path="reports/xnt" element={guard(<XNTReportPage />, 'REPORT_VIEW')} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Toast />
    </>
  );
}
