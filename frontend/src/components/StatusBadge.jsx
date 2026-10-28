import { RECEIPT_STATUS } from '../utils/constants';

export default function StatusBadge({ status, map = RECEIPT_STATUS }) {
  const s = map[status] || { label: status, tone: 'gray' };
  return <span className={`badge badge-${s.tone}`}>{s.label}</span>;
}
