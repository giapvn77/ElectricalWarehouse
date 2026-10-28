import { Link, useParams } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { StockInApi } from '../../api/StockInApi';
import DataTable from '../../components/DataTable';
import DocumentActions from '../../components/DocumentActions';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { hasPermission, selectUser } from '../../store/AuthStore';
import { formatDate, formatMoney, formatNumber } from '../../utils/format';
import DocumentHeader from '../shared/DocumentHeader';

export default function StockInDetailPage() {
  const { id } = useParams();
  const user = useSelector(selectUser);
  const { data: doc, setData, loading, error } = useAsync(() => StockInApi.get(id), [id]);

  if (loading) return <p className="muted">Đang tải...</p>;
  if (error) return <div className="alert alert-error">{error}</div>;

  return (
    <>
      <PageHeader title={`Phiếu nhập ${doc.code}`} subtitle={`${doc.warehouseName} · ${doc.supplierName || 'Không có NCC'} · ${formatDate(doc.receiptDate)}`}
        actions={
          <DocumentActions api={StockInApi} doc={doc} approvePermission="STOCK_IN_APPROVE" onChanged={setData}
            extra={doc.status === 'APPROVED' && hasPermission(user, 'STOCK_IN_CREATE') && (
              <Link className="btn btn-primary" to={`/stock-in/${doc.id}/scan`}>Nhận hàng & quét serial</Link>
            )} />
        } />
      <div className="card">
        <DocumentHeader doc={doc}>
          {doc.invoiceNumber && <div><span className="muted">Số hóa đơn</span>{doc.invoiceNumber}</div>}
        </DocumentHeader>
      </div>
      <div className="card">
        <DataTable rows={doc.details}
          columns={[
            { key: 'sku', title: 'Mã hàng' },
            { key: 'productName', title: 'Tên hàng' },
            { key: 'locationCode', title: 'Vị trí' },
            { key: 'lotNumber', title: 'Số lô' },
            { key: 'expiryDate', title: 'Hạn dùng', render: (d) => formatDate(d.expiryDate) },
            { key: 'quantity', title: 'Số lượng', align: 'right', render: (d) => formatNumber(d.quantity) },
            { key: 'scannedQuantity', title: 'Đã quét serial', align: 'right', render: (d) => (d.serialManaged ? `${d.scannedQuantity}/${d.quantity}` : '—') },
            { key: 'unitPrice', title: 'Đơn giá', align: 'right', render: (d) => formatMoney(d.unitPrice) },
            { key: 'amount', title: 'Thành tiền', align: 'right', render: (d) => formatMoney(d.unitPrice * d.quantity) },
          ]} />
        <p className="right"><b>Tổng tiền: {formatMoney(doc.totalAmount)}</b></p>
      </div>
    </>
  );
}
