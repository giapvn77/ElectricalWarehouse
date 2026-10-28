import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { StockInApi } from '../../api/StockInApi';
import BarcodeScanner from '../../components/BarcodeScanner';
import ConfirmDialog from '../../components/ConfirmDialog';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { useToast } from '../../hooks/useToast';

/** Receiving an approved stock-in: scan a serial for every unit, then post the stock. */
export default function StockInScanPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { data: doc, setData, loading, error, reload } = useAsync(() => StockInApi.get(id), [id]);
  const [detailId, setDetailId] = useState(null);
  const [serials, setSerials] = useState([]);
  const [confirming, setConfirming] = useState(false);

  const serialLines = useMemo(() => doc?.details.filter((d) => d.serialManaged) || [], [doc]);
  const current = serialLines.find((d) => d.id === detailId);

  useEffect(() => {
    if (!detailId && serialLines.length) setDetailId(serialLines.find((d) => d.scannedQuantity < d.quantity)?.id ?? serialLines[0].id);
  }, [serialLines, detailId]);

  useEffect(() => {
    if (detailId) StockInApi.serialsOfDetail(detailId).then(setSerials).catch(() => setSerials([]));
  }, [detailId, doc]);

  const onScan = useCallback(async (code) => {
    if (!detailId) return;
    try {
      await StockInApi.scanSerials(id, detailId, [code]);
      toast.success('Đã quét ' + code);
      reload();
    } catch (e) {
      toast.error(e);
    }
  }, [id, detailId, toast, reload]);

  const remove = async (serial) => {
    try {
      await StockInApi.removeSerial(id, serial.id);
      reload();
    } catch (e) {
      toast.error(e);
    }
  };

  const complete = async () => {
    try {
      setData(await StockInApi.complete(id));
      toast.success('Đã nhập kho thành công');
      navigate(`/stock-in/${id}`);
    } catch (e) {
      toast.error(e);
      setConfirming(false);
    }
  };

  if (loading && !doc) return <p className="muted">Đang tải...</p>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (doc.status !== 'APPROVED') return <div className="alert">Phiếu {doc.code} không ở trạng thái chờ nhận hàng.</div>;

  const ready = serialLines.every((d) => d.scannedQuantity === d.quantity);

  return (
    <>
      <PageHeader title={`Nhận hàng - ${doc.code}`} subtitle={doc.warehouseName}
        actions={<button className="btn btn-success" disabled={!ready} onClick={() => setConfirming(true)}>Hoàn tất nhập kho</button>} />
      {serialLines.length === 0 ? (
        <div className="card">Phiếu không có hàng quản lý serial. Kiểm đếm hàng thực tế rồi bấm “Hoàn tất nhập kho”.</div>
      ) : (
        <div className="grid-2">
          <div className="card">
            <h2>Dòng hàng cần quét</h2>
            {serialLines.map((d) => (
              <button key={d.id} className={`line ${d.id === detailId ? 'line-active' : ''}`} onClick={() => setDetailId(d.id)}>
                <span><b>{d.sku}</b> {d.productName}</span>
                <span className={`badge badge-${d.scannedQuantity === d.quantity ? 'green' : 'amber'}`}>{d.scannedQuantity}/{d.quantity}</span>
              </button>
            ))}
          </div>
          <div className="card">
            <h2>{current ? `Quét serial: ${current.sku}` : 'Chọn dòng hàng'}</h2>
            {current && current.scannedQuantity < current.quantity && <BarcodeScanner onScan={onScan} placeholder="Quét số serial" />}
            <ul className="serials">
              {serials.map((s) => (
                <li key={s.id}><code>{s.serialNumber}</code><button className="btn btn-sm btn-ghost" onClick={() => remove(s)}>✕</button></li>
              ))}
            </ul>
          </div>
        </div>
      )}
      <ConfirmDialog open={confirming} title="Hoàn tất nhập kho" message="Xác nhận đã nhận đủ hàng? Tồn kho sẽ được cộng ngay."
        onCancel={() => setConfirming(false)} onConfirm={complete} />
    </>
  );
}
