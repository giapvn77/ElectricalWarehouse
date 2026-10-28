import { useCallback, useState } from 'react';
import { useDispatch } from 'react-redux';
import { ReportApi } from '../../api/ReportApi';
import DataTable from '../../components/DataTable';
import PageHeader from '../../components/PageHeader';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { fetchUnreadCount } from '../../store/NotificationStore';
import { formatDateTime } from '../../utils/format';

export default function NotificationsPage() {
  const dispatch = useDispatch();
  const [filters] = useState({});
  const fetcher = useCallback((p) => ReportApi.notifications(p), []);
  const { data, loading, error, setPage, reload } = usePagedQuery(fetcher, filters);

  const markRead = async (n) => {
    if (n.read) return;
    await ReportApi.markRead(n.id);
    reload();
    dispatch(fetchUnreadCount());
  };

  const markAll = async () => {
    await ReportApi.markAllRead();
    reload();
    dispatch(fetchUnreadCount());
  };

  return (
    <>
      <PageHeader title="Thông báo" actions={<button className="btn" onClick={markAll}>Đánh dấu đã đọc tất cả</button>} />
      <div className="card">
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage} onRowClick={markRead}
          columns={[
            { key: 'read', title: '', width: 24, render: (n) => (n.read ? '' : <span className="dot" />) },
            { key: 'title', title: 'Tiêu đề', render: (n) => (n.read ? n.title : <b>{n.title}</b>) },
            { key: 'message', title: 'Nội dung' },
            { key: 'createdAt', title: 'Thời gian', render: (n) => formatDateTime(n.createdAt) },
          ]} />
      </div>
    </>
  );
}
