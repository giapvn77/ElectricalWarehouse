import { useSelector } from 'react-redux';
import { Link, useParams } from 'react-router-dom';
import { StockOutApi } from '../../api/StockOutApi';
import DataTable from '../../components/DataTable';
import DocumentActions from '../../components/DocumentActions';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { hasPermission, hasRole, selectUser } from '../../store/AuthStore';
import { useToast } from '../../hooks/useToast';
import { OUT_TYPE } from '../../utils/constants';
import { formatDate, formatMoney, formatNumber } from '../../utils/format';
import DocumentHeader from '../shared/DocumentHeader';

export default function StockOutDetailPage() {
  const { id } = useParams();
  const user = useSelector(selectUser);
  const toast = useToast();
  const { data: doc, setData, loading, error } = useAsync(() => StockOutApi.get(id), [id]);

  if (loading) return <p className="muted">Đang tải...</p>;
  if (error) return <div className="alert alert-error">{error}</div>;

  const canPick = ['APPROVED', 'PICKING'].includes(doc.status) && hasPermission(user, 'STOCK_OUT_PICK');
  // Approved receipts hold reserved stock; only managers may cancel them.
  const canCancelApproved = ['APPROVED', 'PICKING'].includes(doc.status) && hasRole(user, 'ADMIN', 'QLK');

  const cancelApproved = async () => {
    try {
      setData(await StockOutApi.cancel(doc.id));
      toast.success('Đã hủy phiếu và trả lại hàng giữ chỗ');
    } catch (e) {
      toast.error(e);
    }
  };

  return (
    <>
      <PageHeader title={`Phiếu xuất ${doc.code}`} subtitle={`${doc.warehouseName} · ${OUT_TYPE[doc.outType]} · ${doc.customerName || 'Không có khách hàng'} · ${formatDate(doc.receiptDate)}`}
        actions={
          <DocumentActions api={StockOutApi} doc={doc} approvePermission="STOCK_OUT_APPROVE" onChanged={setData}
            extra={
              <>
                {canPick && <Link className="btn btn-primary" to={`/stock-out/${doc.id}/pick`}>Lấy hàng</Link>}
                {canCancelApproved && <button className="btn" onClick={cancelApproved}>Hủy & trả giữ chỗ</button>}
              </>
            } />
        } />
      <div className="card">
        <DocumentHeader doc={doc}>
          {doc.pickedByName && <div><span className="muted">Người lấy hàng</span>{doc.pickedByName}</div>}
        </DocumentHeader>
      </div>
      <div className="card">
        <DataTable rows={doc.details}
          columns={[
            { key: 'sku', title: 'Mã hàng' },
            { key: 'productName', title: 'Tên hàng' },
            { key: 'quantity', title: 'Số lượng', align: 'right', render: (d) => formatNumber(d.quantity) },
            { key: 'pickedQuantity', title: 'Đã lấy', align: 'right', render: (d) => `${d.pickedQuantity}/${d.quantity}` },
            { key: 'serialNumbers', title: 'Serial', render: (d) => d.serialNumbers?.join(', ') },
            { key: 'unitPrice', title: 'Đơn giá', align: 'right', render: (d) => formatMoney(d.unitPrice) },
            { key: 'amount', title: 'Thành tiền', align: 'right', render: (d) => formatMoney(d.unitPrice * d.quantity) },
          ]} />
        <p className="right"><b>Tổng tiền: {formatMoney(doc.totalAmount)}</b></p>
      </div>
    </>
  );
}
