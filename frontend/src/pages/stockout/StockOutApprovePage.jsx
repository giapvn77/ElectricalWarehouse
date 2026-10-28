import { StockOutApi } from '../../api/StockOutApi';
import PageHeader from '../../components/PageHeader';
import ApprovalQueue from '../shared/ApprovalQueue';

export default function StockOutApprovePage() {
  return (
    <>
      <PageHeader title="Duyệt phiếu xuất" subtitle="Duyệt phiếu sẽ giữ chỗ hàng trong kho cho đến khi xuất hoặc hủy." />
      <ApprovalQueue api={StockOutApi} basePath="/stock-out" approvePermission="STOCK_OUT_APPROVE" partnerKey="customerName" />
    </>
  );
}
