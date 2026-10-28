import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { InventoryApi } from '../../api/InventoryApi';
import { StockInApi } from '../../api/StockInApi';
import { SupplierApi } from '../../api/SupplierApi';
import BarcodeScanner from '../../components/BarcodeScanner';
import PageHeader from '../../components/PageHeader';
import ProductPicker from '../../components/ProductPicker';
import WarehouseSelect from '../../components/WarehouseSelect';
import { useToast } from '../../hooks/useToast';
import { clearCart, removeItem, updateItem } from '../../store/CartStore';
import { formatMoney, getErrorMessage, today } from '../../utils/format';
import { useScanToCart } from '../shared/useScanToCart';

const CART = 'stockIn';

export default function StockInCreatePage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const toast = useToast();
  const items = useSelector((s) => s.cart[CART]);
  const { addProduct, scan } = useScanToCart(CART, 'costPrice');
  const [header, setHeader] = useState({ warehouseId: null, supplierId: '', receiptDate: today(), invoiceNumber: '', note: '' });
  const [suppliers, setSuppliers] = useState([]);
  const [locations, setLocations] = useState([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    SupplierApi.search({ size: 200 }).then((p) => setSuppliers(p.content.filter((s) => s.active))).catch(() => {});
  }, []);
  useEffect(() => {
    if (header.warehouseId) InventoryApi.locations(header.warehouseId).then(setLocations).catch(() => setLocations([]));
    else setLocations([]);
  }, [header.warehouseId]);

  const update = (key, changes) => dispatch(updateItem({ cart: CART, key, changes }));
  const total = items.reduce((sum, i) => sum + Number(i.quantity || 0) * Number(i.unitPrice || 0), 0);

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const receipt = await StockInApi.create({
        ...header,
        supplierId: header.supplierId ? Number(header.supplierId) : null,
        items: items.map((i) => ({
          productId: i.productId, locationId: i.locationId ? Number(i.locationId) : null, quantity: Number(i.quantity),
          unitPrice: Number(i.unitPrice), lotNumber: i.lotNumber || null, expiryDate: i.expiryDate || null,
        })),
      });
      dispatch(clearCart({ cart: CART }));
      toast.success('Đã tạo phiếu nhập ' + receipt.code);
      navigate(`/stock-in/${receipt.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={save}>
      <PageHeader title="Tạo phiếu nhập kho" actions={
        <>
          <button type="button" className="btn" onClick={() => dispatch(clearCart({ cart: CART }))}>Xóa danh sách</button>
          <button className="btn btn-primary" disabled={saving || items.length === 0}>{saving ? 'Đang lưu...' : 'Lưu nháp'}</button>
        </>
      } />
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card form-grid">
        <label className="field"><span>Kho nhập<b className="req">*</b></span>
          <WarehouseSelect required value={header.warehouseId} onChange={(v) => setHeader({ ...header, warehouseId: v })} />
        </label>
        <label className="field"><span>Nhà cung cấp</span>
          <select value={header.supplierId} onChange={(e) => setHeader({ ...header, supplierId: e.target.value })}>
            <option value="">-- Chọn --</option>
            {suppliers.map((s) => <option key={s.id} value={s.id}>{s.code} - {s.name}</option>)}
          </select>
        </label>
        <label className="field"><span>Ngày nhập</span>
          <input type="date" value={header.receiptDate} onChange={(e) => setHeader({ ...header, receiptDate: e.target.value })} />
        </label>
        <label className="field"><span>Số hóa đơn</span>
          <input value={header.invoiceNumber} onChange={(e) => setHeader({ ...header, invoiceNumber: e.target.value })} />
        </label>
        <label className="field field-full"><span>Ghi chú</span>
          <input value={header.note} onChange={(e) => setHeader({ ...header, note: e.target.value })} />
        </label>
      </div>

      <div className="card">
        <div className="grid-2">
          <ProductPicker onSelect={addProduct} />
          <BarcodeScanner autoFocus={false} onScan={scan} placeholder="Quét mã vạch sản phẩm" />
        </div>
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>Sản phẩm</th><th>Vị trí</th><th>Số lượng</th><th>Đơn giá</th><th>Số lô</th><th>Hạn dùng</th><th className="right">Thành tiền</th><th /></tr>
            </thead>
            <tbody>
              {items.length === 0 && <tr><td colSpan={8} className="muted center">Chưa có sản phẩm</td></tr>}
              {items.map((i) => (
                <tr key={i.key}>
                  <td><b>{i.sku}</b> {i.name} {i.serialManaged && <span className="badge badge-violet">Serial</span>}</td>
                  <td>
                    <select value={i.locationId || ''} onChange={(e) => update(i.key, { locationId: e.target.value })}>
                      <option value="">--</option>
                      {locations.map((l) => <option key={l.id} value={l.id}>{l.code}</option>)}
                    </select>
                  </td>
                  <td><input type="number" min="1" className="w-sm" required value={i.quantity} onChange={(e) => update(i.key, { quantity: e.target.value })} /></td>
                  <td><input type="number" min="0" className="w-md" value={i.unitPrice} onChange={(e) => update(i.key, { unitPrice: e.target.value })} /></td>
                  <td><input className="w-md" value={i.lotNumber || ''} onChange={(e) => update(i.key, { lotNumber: e.target.value })} /></td>
                  <td><input type="date" required={i.expiryManaged} value={i.expiryDate || ''} onChange={(e) => update(i.key, { expiryDate: e.target.value })} /></td>
                  <td className="right">{formatMoney(Number(i.quantity) * Number(i.unitPrice))}</td>
                  <td><button type="button" className="btn btn-sm btn-ghost" onClick={() => dispatch(removeItem({ cart: CART, key: i.key }))}>✕</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="right"><b>Tổng tiền: {formatMoney(total)}</b></p>
      </div>
    </form>
  );
}
