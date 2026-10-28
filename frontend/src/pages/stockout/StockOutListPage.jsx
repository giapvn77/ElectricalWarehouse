import { useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { StockOutApi } from '../../api/StockOutApi';
import PageHeader from '../../components/PageHeader';
import { hasPermission, selectUser } from '../../store/AuthStore';
import { OUT_TYPE } from '../../utils/constants';
import DocumentList from '../shared/DocumentList';

export default function StockOutListPage() {
  const user = useSelector(selectUser);
  return (
    <>
      <PageHeader title="Phiếu xuất kho" actions={hasPermission(user, 'STOCK_OUT_CREATE') && <Link className="btn btn-primary" to="/stock-out/new">+ Tạo phiếu xuất</Link>} />
      <DocumentList api={StockOutApi} basePath="/stock-out" partnerKey="customerName" partnerTitle="Khách hàng"
        extraColumns={[{ key: 'outType', title: 'Loại xuất', render: (r) => OUT_TYPE[r.outType] }]} />
    </>
  );
}
