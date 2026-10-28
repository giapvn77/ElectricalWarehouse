import { useWarehouses } from '../hooks/useWarehouses';

export default function WarehouseSelect({ value, onChange, allowAll, exclude, required }) {
  const warehouses = useWarehouses();
  return (
    <select value={value ?? ''} required={required} onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}>
      <option value="">{allowAll ? 'Tất cả kho' : '-- Chọn kho --'}</option>
      {warehouses.filter((w) => w.id !== exclude).map((w) => (
        <option key={w.id} value={w.id}>{w.code} - {w.name}</option>
      ))}
    </select>
  );
}
