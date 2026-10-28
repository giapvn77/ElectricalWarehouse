import { useCallback, useState } from 'react';
import { useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { ProductApi } from '../../api/ProductApi';
import DataTable from '../../components/DataTable';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { usePagedQuery } from '../../hooks/usePagedQuery';
import { hasPermission, selectUser } from '../../store/AuthStore';
import { formatMoney } from '../../utils/format';

export default function ProductListPage() {
  const navigate = useNavigate();
  const user = useSelector(selectUser);
  const categories = useAsync(() => ProductApi.categories(), []);
  const [form, setForm] = useState({ keyword: '', categoryId: '' });
  const [filters, setFilters] = useState({});
  const fetcher = useCallback((p) => ProductApi.search(p), []);
  const { data, loading, error, setPage } = usePagedQuery(fetcher, filters);

  return (
    <>
      <PageHeader title="Sản phẩm" actions={hasPermission(user, 'CATALOG_MANAGE') && <Link className="btn btn-primary" to="/products/new">+ Thêm sản phẩm</Link>} />
      <div className="card">
        <form className="filters" onSubmit={(e) => { e.preventDefault(); setFilters({ ...form }); }}>
          <input placeholder="Mã SKU, tên, mã vạch" value={form.keyword} onChange={(e) => setForm({ ...form, keyword: e.target.value })} />
          <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
            <option value="">Tất cả nhóm hàng</option>
            {categories.data?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <button className="btn">Tìm</button>
        </form>
        <DataTable loading={loading} error={error} rows={data.content} page={data} onPageChange={setPage}
          onRowClick={(p) => navigate(`/products/${p.id}`)}
          columns={[
            { key: 'sku', title: 'Mã SKU' },
            { key: 'name', title: 'Tên sản phẩm' },
            { key: 'categoryName', title: 'Nhóm hàng' },
            { key: 'unitName', title: 'ĐVT' },
            { key: 'brand', title: 'Thương hiệu' },
            { key: 'flags', title: 'Quản lý', render: (p) => (
              <>
                {p.serialManaged && <span className="badge badge-violet">Serial</span>}{' '}
                {p.expiryManaged && <span className="badge badge-amber">HSD</span>}
              </>
            ) },
            { key: 'salePrice', title: 'Giá bán', align: 'right', render: (p) => formatMoney(p.salePrice) },
            { key: 'active', title: 'Trạng thái', render: (p) => <span className={`badge badge-${p.active ? 'green' : 'gray'}`}>{p.active ? 'Đang KD' : 'Ngừng KD'}</span> },
          ]} />
      </div>
    </>
  );
}
