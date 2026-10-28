import { CustomerApi } from '../../api/SupplierApi';
import PartnerManager from './PartnerManager';

export default function CustomerPage() {
  return <PartnerManager title="Khách hàng" api={CustomerApi} />;
}
