import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { InventoryApi } from '../../api/InventoryApi';
import { StockOutApi } from '../../api/StockOutApi';
import { CustomerApi } from '../../api/SupplierApi';
import BarcodeScanner from '../../components/BarcodeScanner';
import PageHeader from '../../components/PageHeader';
import ProductPicker from '../../components/ProductPicker';
import WarehouseSelect from '../../components/WarehouseSelect';
import { useToast } from '../../hooks/useToast';
import { clearCart, removeItem, updateItem } from '../../store/CartStore';
import { OUT_TYPE, toOptions } from '../../utils/constants';
import { formatMoney, formatNumber, getErrorMessage, today } from '../../utils/format';
import { useScanToCart } from '../shared/useScanToCart';

const CART = 'stockOut';

export default function StockOutCreatePage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const toast = useToast();
  const items = useSelector((s) => s.cart[CART]);
  const { addProduct, scan } = useScanToCart(CART, 'salePrice');
  const [header, setHeader] = useState({ warehouseId: null, customerId: '', outType: 'SALE', receiptDate: today(), note: '' });
  const [customers, setCustomers] = useState([]);
  const [available, setAvailable] = useState({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    CustomerApi.search({ size: 200 }).then((p) => setCustomers(p.content.filter((c) => c.active))).catch(() => {});
  }, []);

  // Show available stock for each line in the chosen warehouse.
  const productIds = items.map((i) => i.productId).join(',');
  useEffect(() => {
    if (!header.warehouseId || !productIds) return;
    const ids = [...new Set(productIds.split(',').map(Number))];
    Promise.all(ids.map((pid) => InventoryApi.available(header.warehouseId, pid).then((q) => [pid, q])))
      .then((pairs) => setAvailable(Object.fromEntries(pairs)))
      .catch(() => {});
  }, [header.warehouseId, productIds]);

  const update = (key, changes) => dispatch(updateItem({ cart: CART, key, changes }));
  const total = items.reduce((sum, i) => sum + Number(i.quantity || 0) * Number(i.unitPrice || 0), 0);

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const receipt = await StockOutApi.create({
        ...header,
        customerId: header.customerId ? Number(header.customerId) : null,
        items: items.map((i) => ({ productId: i.productId, quantity: Number(i.quantity), unitPrice: Number(i.unitPrice) })),
      });
      dispatch(clearCart({ cart: CART }));
      toast.success('Đã tạo phiếu xuất ' + receipt.code);
      navigate(`/stock-out/${receipt.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={save}>
      <PageHeader title="Tạo phiếu xuất kho" actions={
        <>
          <button type="button" className="btn" onClick={() => dispatch(clearCart({ cart: CART }))}>Xóa danh sách</button>
          <button className="btn btn-primary" disabled={saving || items.length === 0}>{saving ? 'Đang lưu...' : 'Lưu nháp'}</button>
        </>
      } />
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card form-grid">
        <label className="field"><span>Kho xuất<b className="req">*</b></span>
          <WarehouseSelect required value={header.warehouseId} onChange={(v) => setHeader({ ...header, warehouseId: v })} />
        </label>
        <label className="field"><span>Loại xuất</span>
          <select value={header.outType} onChange={(e) => setHeader({ ...header, outType: e.target.value })}>
            {toOptions(OUT_TYPE).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </label>
        <label className="field"><span>Khách hàng</span>
          <select value={header.customerId} onChange={(e) => setHeader({ ...header, customerId: e.target.value })}>
            <option value="">-- Chọn --</option>
            {customers.map((c) => <option key={c.id} value={c.id}>{c.code} - {c.name}</option>)}
          </select>
        </label>
        <label className="field"><span>Ngày xuất</span>
          <input type="date" value={header.receiptDate} onChange={(e) => setHeader({ ...header, receiptDate: e.target.value })} />
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
              <tr><th>Sản phẩm</th><th className="right">Tồn khả dụng</th><th>Số lượng</th><th>Đơn giá</th><th className="right">Thành tiền</th><th /></tr>
            </thead>
            <tbody>
              {items.length === 0 && <tr><td colSpan={6} className="muted center">Chưa có sản phẩm</td></tr>}
              {items.map((i) => {
                const avail = available[i.productId];
                const short = header.warehouseId && avail != null && Number(i.quantity) > avail;
                return (
                  <tr key={i.key}>
                    <td><b>{i.sku}</b> {i.name}</td>
                    <td className={`right ${short ? 'error' : ''}`}>{header.warehouseId ? formatNumber(avail) : '—'}</td>
                    <td><input type="number" min="1" className="w-sm" required value={i.quantity} onChange={(e) => update(i.key, { quantity: e.target.value })} /></td>
                    <td><input type="number" min="0" className="w-md" value={i.unitPrice} onChange={(e) => update(i.key, { unitPrice: e.target.value })} /></td>
                    <td className="right">{formatMoney(Number(i.quantity) * Number(i.unitPrice))}</td>
                    <td><button type="button" className="btn btn-sm btn-ghost" onClick={() => dispatch(removeItem({ cart: CART, key: i.key }))}>✕</button></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        <p className="right"><b>Tổng tiền: {formatMoney(total)}</b></p>
      </div>
    </form>
  );
}
