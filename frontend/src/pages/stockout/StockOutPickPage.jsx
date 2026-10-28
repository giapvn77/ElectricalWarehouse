import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { StockOutApi } from '../../api/StockOutApi';
import BarcodeScanner from '../../components/BarcodeScanner';
import ConfirmDialog from '../../components/ConfirmDialog';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { useToast } from '../../hooks/useToast';
import { formatDate } from '../../utils/format';

/** Picking for an approved stock-out, guided by FIFO/FEFO location suggestions. */
export default function StockOutPickPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const doc = useAsync(() => StockOutApi.get(id), [id]);
  const pickList = useAsync(() => StockOutApi.pickList(id), [id]);
  const [quantities, setQuantities] = useState({});
  const [confirming, setConfirming] = useState(false);

  const reloadAll = () => {
    doc.reload();
    pickList.reload();
  };

  const pick = async (detailId, body) => {
    try {
      await StockOutApi.pick(id, { detailId, quantity: 0, ...body });
      toast.success('Đã ghi nhận lấy hàng');
      reloadAll();
    } catch (e) {
      toast.error(e);
    }
  };

  const complete = async () => {
    try {
      await StockOutApi.complete(id);
      toast.success('Đã xuất kho');
      navigate(`/stock-out/${id}`);
    } catch (e) {
      toast.error(e);
      setConfirming(false);
    }
  };

  if (doc.loading || pickList.loading) return <p className="muted">Đang tải...</p>;
  if (doc.error || pickList.error) return <div className="alert alert-error">{doc.error || pickList.error}</div>;

  const detailById = Object.fromEntries(doc.data.details.map((d) => [d.id, d]));
  const allPicked = doc.data.details.every((d) => d.pickedQuantity === d.quantity);

  return (
    <>
      <PageHeader title={`Lấy hàng - ${doc.data.code}`} subtitle={doc.data.warehouseName}
        actions={<button className="btn btn-success" disabled={!allPicked || doc.data.status !== 'PICKING'} onClick={() => setConfirming(true)}>Hoàn tất xuất kho</button>} />
      {pickList.data.map((line) => {
        const detail = detailById[line.detailId];
        return (
          <div className="card" key={line.detailId}>
            <div className="card-header">
              <h2>{line.sku} - {line.productName}</h2>
              <span className={`badge badge-${detail.pickedQuantity === detail.quantity ? 'green' : 'amber'}`}>Đã lấy {detail.pickedQuantity}/{detail.quantity}</span>
            </div>
            <table className="table">
              <thead><tr><th>Vị trí</th><th>Lô</th><th>Hạn dùng</th><th className="right">Tồn</th><th className="right">Gợi ý lấy</th></tr></thead>
              <tbody>
                {line.locations.length === 0 && <tr><td colSpan={5} className="muted">Không còn tồn kho</td></tr>}
                {line.locations.map((l) => (
                  <tr key={l.inventoryId} className={l.suggestedQuantity > 0 ? 'highlight' : ''}>
                    <td>{l.locationCode || '—'}</td><td>{l.lotNumber || '—'}</td><td>{formatDate(l.expiryDate)}</td>
                    <td className="right">{l.availableQuantity}</td><td className="right"><b>{l.suggestedQuantity || ''}</b></td>
                  </tr>
                ))}
              </tbody>
            </table>
            {line.serialManaged ? (
              detail.pickedQuantity < detail.quantity && (
                <BarcodeScanner autoFocus={false} placeholder="Quét serial đã lấy" onScan={(code) => pick(line.detailId, { serialNumbers: [code] })} />
              )
            ) : (
              <div className="row">
                <input type="number" min="0" max={detail.quantity} className="w-sm"
                  value={quantities[line.detailId] ?? detail.quantity}
                  onChange={(e) => setQuantities({ ...quantities, [line.detailId]: e.target.value })} />
                <button className="btn btn-primary" onClick={() => pick(line.detailId, { quantity: Number(quantities[line.detailId] ?? detail.quantity) })}>
                  Xác nhận đã lấy
                </button>
              </div>
            )}
          </div>
        );
      })}
      <ConfirmDialog open={confirming} title="Hoàn tất xuất kho" message="Xác nhận hàng đã được lấy đủ và giao đi? Tồn kho sẽ bị trừ."
        onCancel={() => setConfirming(false)} onConfirm={complete} />
    </>
  );
}
