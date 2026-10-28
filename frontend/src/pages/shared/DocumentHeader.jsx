import StatusBadge from '../../components/StatusBadge';
import { formatDateTime } from '../../utils/format';

/** Common facts of an approval document: status, creator, approver, reject reason. */
export default function DocumentHeader({ doc, children }) {
  return (
    <div className="doc-header">
      <div><span className="muted">Trạng thái</span><StatusBadge status={doc.status} /></div>
      <div><span className="muted">Người lập</span>{doc.createdByName} · {formatDateTime(doc.createdAt)}</div>
      {doc.approvedByName && (
        <div><span className="muted">{doc.status === 'REJECTED' ? 'Người từ chối' : 'Người duyệt'}</span>{doc.approvedByName} · {formatDateTime(doc.approvedAt)}</div>
      )}
      {children}
      {doc.rejectReason && <div className="alert alert-error">Lý do từ chối: {doc.rejectReason}</div>}
      {doc.note && <div><span className="muted">Ghi chú</span>{doc.note}</div>}
    </div>
  );
}
