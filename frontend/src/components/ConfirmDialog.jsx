import { useState } from 'react';
import Modal from './Modal';

/** Confirmation popup. With requireReason it collects a reason (e.g. when rejecting a document). */
export default function ConfirmDialog({ open, title = 'Xác nhận', message, confirmLabel = 'Đồng ý', danger, requireReason, onConfirm, onCancel }) {
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);

  const confirm = async () => {
    setBusy(true);
    try {
      await onConfirm(requireReason ? reason.trim() : undefined);
      setReason('');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal open={open} title={title} onClose={onCancel} width={460}>
      <p>{message}</p>
      {requireReason && (
        <textarea rows={3} className="w-full" placeholder="Nhập lý do..." value={reason} onChange={(e) => setReason(e.target.value)} />
      )}
      <div className="form-actions">
        <button className="btn" onClick={onCancel}>Hủy</button>
        <button className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`} disabled={busy || (requireReason && !reason.trim())} onClick={confirm}>
          {confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
