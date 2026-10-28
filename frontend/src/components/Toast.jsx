import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { dismissToast } from '../store/NotificationStore';

export default function Toast() {
  const toasts = useSelector((s) => s.notifications.toasts);
  return (
    <div className="toasts" aria-live="polite">
      {toasts.map((t) => <ToastItem key={t.id} toast={t} />)}
    </div>
  );
}

function ToastItem({ toast }) {
  const dispatch = useDispatch();
  useEffect(() => {
    const timer = setTimeout(() => dispatch(dismissToast(toast.id)), toast.type === 'error' ? 6000 : 3000);
    return () => clearTimeout(timer);
  }, [dispatch, toast]);
  return (
    <div className={`toast toast-${toast.type}`} onClick={() => dispatch(dismissToast(toast.id))}>
      {toast.message}
    </div>
  );
}
