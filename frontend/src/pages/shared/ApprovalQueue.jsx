import { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import DataTable from '../../components/DataTable';
import DocumentActions from '../../components/DocumentActions';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { formatDateTime, formatMoney, formatNumber } from '../../utils/format';

/** PENDING documents with inline line items and approve / reject buttons. */
export default function ApprovalQueue({ api, basePath, approvePermission, partnerKey }) {
  const [filters] = useState({ status: 'PENDING' });
  const fetcher = useCallback((p) => api.search(p), [api]);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters);

  if (!loading && data.content.length === 0) return <div className="card muted">Không có phiếu nào chờ duyệt.</div>;

  return (
    <>
      {error && <div className="alert alert-error">{error}</div>}
      {data.content.map((doc) => (
        <div className="card" key={doc.id}>
          <div className="card-header">
            <div>
              <h2><Link to={`${basePath}/${doc.id}`}>{doc.code}</Link></h2>
              <p className="muted">
                {doc.warehouseName} · {doc[partnerKey] || '—'} · Lập bởi {doc.createdByName} lúc {formatDateTime(doc.createdAt)}
              </p>
            </div>
            <DocumentActions api={api} doc={doc} approvePermission={approvePermission} onChanged={reload} />
          </div>
          <DataTable rows={doc.details}
            columns={[
              { key: 'sku', title: 'Mã hàng' },
              { key: 'productName', title: 'Tên hàng' },
              { key: 'quantity', title: 'Số lượng', align: 'right', render: (d) => formatNumber(d.quantity) },
              { key: 'unitPrice', title: 'Đơn giá', align: 'right', render: (d) => formatMoney(d.unitPrice) },
            ]} />
          <p className="right"><b>Tổng: {formatMoney(doc.totalAmount)}</b></p>
        </div>
      ))}
      {data.totalPages > 1 && (
        <div className="pagination">
          <button className="btn btn-sm" disabled={data.page === 0} onClick={() => setPage(data.page - 1)}>‹ Trước</button>
          <span>Trang {data.page + 1}/{data.totalPages}</span>
          <button className="btn btn-sm" disabled={data.page + 1 >= data.totalPages} onClick={() => setPage(data.page + 1)}>Sau ›</button>
        </div>
      )}
    </>
  );
}
