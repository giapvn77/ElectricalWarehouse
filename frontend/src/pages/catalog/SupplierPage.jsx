import { SupplierApi } from '../../api/SupplierApi';
import PartnerManager from './PartnerManager';

export default function SupplierPage() {
  return <PartnerManager title="Nhà cung cấp" api={SupplierApi} />;
}
