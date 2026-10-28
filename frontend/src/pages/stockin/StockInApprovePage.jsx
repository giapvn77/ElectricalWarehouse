import { StockInApi } from '../../api/StockInApi';
import PageHeader from '../../components/PageHeader';
import ApprovalQueue from '../shared/ApprovalQueue';

export default function StockInApprovePage() {
  return (
    <>
      <PageHeader title="Duyệt phiếu nhập" subtitle="Bạn không thể duyệt phiếu do chính mình lập." />
      <ApprovalQueue api={StockInApi} basePath="/stock-in" approvePermission="STOCK_IN_APPROVE" partnerKey="supplierName" />
    </>
  );
}
