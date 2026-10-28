import { useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { StockInApi } from '../../api/StockInApi';
import PageHeader from '../../components/PageHeader';
import { hasPermission, selectUser } from '../../store/AuthStore';
import DocumentList from '../shared/DocumentList';

export default function StockInListPage() {
  const user = useSelector(selectUser);
  return (
    <>
      <PageHeader title="Phiếu nhập kho" actions={hasPermission(user, 'STOCK_IN_CREATE') && <Link className="btn btn-primary" to="/stock-in/new">+ Tạo phiếu nhập</Link>} />
      <DocumentList api={StockInApi} basePath="/stock-in" partnerKey="supplierName" partnerTitle="Nhà cung cấp"
        extraColumns={[{ key: 'invoiceNumber', title: 'Số hóa đơn' }]} />
    </>
  );
}
