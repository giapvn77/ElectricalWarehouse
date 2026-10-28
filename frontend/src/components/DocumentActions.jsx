import { useState } from 'react';
import { useSelector } from 'react-redux';
import { hasPermission, hasRole, selectUser } from '../store/AuthStore';
import { useToast } from '../hooks/useToast';
import ConfirmDialog from './ConfirmDialog';

/**
 * Submit / approve / reject / cancel buttons shared by every approval document.
 * Approval is allowed by approvePermission, or by approveRoles when the backend checks roles.
 * api: { submit, approve, reject, cancel }; doc: response with id, code, status, createdById.
 */
export default function DocumentActions({ api, doc, approvePermission, approveRoles, onChanged, extra }) {
  const user = useSelector(selectUser);
  const toast = useToast();
  const [dialog, setDialog] = useState(null);

  const run = async (fn, message) => {
    try {
      const updated = await fn();
      toast.success(message);
      setDialog(null);
      onChanged?.(updated);
    } catch (e) {
      toast.error(e);
    }
  };

  const allowed = approveRoles ? hasRole(user, ...approveRoles) : hasPermission(user, approvePermission);
  const canApprove = allowed && doc.status === 'PENDING';
  const isCreator = doc.createdById === user?.id;

  return (
    <div className="actions">
      {doc.status === 'DRAFT' && (
        <button className="btn btn-primary" onClick={() => run(() => api.submit(doc.id), 'Đã gửi duyệt')}>Gửi duyệt</button>
      )}
      {canApprove && (
        <>
          <button className="btn btn-success" disabled={isCreator} title={isCreator ? 'Người lập phiếu không được tự duyệt' : ''}
            onClick={() => setDialog('approve')}>Duyệt</button>
          <button className="btn btn-danger" disabled={isCreator} onClick={() => setDialog('reject')}>Từ chối</button>
        </>
      )}
      {extra}
      {['DRAFT', 'PENDING'].includes(doc.status) && (
        <button className="btn" onClick={() => setDialog('cancel')}>Hủy phiếu</button>
      )}

      <ConfirmDialog open={dialog === 'approve'} title="Duyệt phiếu" message={`Duyệt phiếu ${doc.code}?`}
        onCancel={() => setDialog(null)} onConfirm={() => run(() => api.approve(doc.id), 'Đã duyệt phiếu')} />
      <ConfirmDialog open={dialog === 'reject'} title="Từ chối phiếu" message={`Từ chối phiếu ${doc.code}?`} danger requireReason
        confirmLabel="Từ chối" onCancel={() => setDialog(null)}
        onConfirm={(reason) => run(() => api.reject(doc.id, reason), 'Đã từ chối phiếu')} />
      <ConfirmDialog open={dialog === 'cancel'} title="Hủy phiếu" message={`Hủy phiếu ${doc.code}? Thao tác không thể hoàn tác.`} danger
        confirmLabel="Hủy phiếu" onCancel={() => setDialog(null)} onConfirm={() => run(() => api.cancel(doc.id), 'Đã hủy phiếu')} />
    </div>
  );
}
