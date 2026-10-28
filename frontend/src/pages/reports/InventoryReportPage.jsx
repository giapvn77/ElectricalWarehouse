import { useState } from 'react';
import { ReportApi } from '../../api/ReportApi';
import DataTable from '../../components/DataTable';
import PageHeader from '../../components/PageHeader';
import WarehouseSelect from '../../components/WarehouseSelect';
import { useAsync } from '../../hooks/useAsync';
import { formatMoney, formatNumber } from '../../utils/format';
import ExportModal from './ExportModal';

export default function InventoryReportPage() {
  const [warehouseId, setWarehouseId] = useState(null);
  const [exporting, setExporting] = useState(false);
  const { data, loading, error } = useAsync(() => ReportApi.inventory(warehouseId), [warehouseId]);

  return (
    <>
      <PageHeader title="Báo cáo tồn kho" actions={<button className="btn" onClick={() => setExporting(true)}>Xuất file</button>} />
      <div className="card">
        <div className="filters"><WarehouseSelect allowAll value={warehouseId} onChange={setWarehouseId} /></div>
        {data && <p><b>Tổng số lượng:</b> {formatNumber(data.totalQuantity)} · <b>Tổng giá trị:</b> {formatMoney(data.totalValue)}</p>}
        <DataTable loading={loading} error={error} rows={data?.rows || []} rowKey="sku"
          columns={[
            { key: 'warehouseName', title: 'Kho' },
            { key: 'sku', title: 'Mã hàng' },
            { key: 'productName', title: 'Tên hàng' },
            { key: 'unitName', title: 'ĐVT' },
            { key: 'quantity', title: 'Tồn', align: 'right', render: (r) => {
              const low = r.minQuantity != null && r.quantity < r.minQuantity;
              const high = r.maxQuantity != null && r.quantity > r.maxQuantity;
              return <b className={low ? 'error' : high ? 'warn' : ''}>{formatNumber(r.quantity)}</b>;
            } },
            { key: 'minMax', title: 'Min / Max', align: 'right', render: (r) => (r.minQuantity != null ? `${r.minQuantity} / ${r.maxQuantity}` : '') },
            { key: 'costPrice', title: 'Giá vốn', align: 'right', render: (r) => formatMoney(r.costPrice) },
            { key: 'value', title: 'Giá trị', align: 'right', render: (r) => formatMoney(r.value) },
          ]} />
      </div>
      <ExportModal open={exporting} onClose={() => setExporting(false)} report="inventory" params={{ warehouseId }} />
    </>
  );
}
