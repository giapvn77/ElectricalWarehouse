import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import http from '../../api/httpClient';
import { ProductApi } from '../../api/ProductApi';
import BaseForm from '../../components/BaseForm';
import FileUploader from '../../components/FileUploader';
import PageHeader from '../../components/PageHeader';
import { useAsync } from '../../hooks/useAsync';
import { useToast } from '../../hooks/useToast';

const EMPTY = { sku: '', name: '', serialManaged: false, expiryManaged: false, active: true, costPrice: 0, salePrice: 0 };

/** Create (/products/new) and edit (/products/:id) a product. */
export default function ProductCreatePage() {
  const { id } = useParams();
  const isNew = !id || id === 'new';
  const navigate = useNavigate();
  const toast = useToast();
  const categories = useAsync(() => ProductApi.categories(), []);
  const units = useAsync(() => ProductApi.units(), []);
  const product = useAsync(() => (isNew ? Promise.resolve(EMPTY) : ProductApi.get(id)), [id]);
  const [barcodeSrc, setBarcodeSrc] = useState(null);

  // Barcode endpoint needs the auth header, so load it as a blob.
  useEffect(() => {
    if (isNew) return undefined;
    let url;
    http.get(ProductApi.barcodeUrl(id), { responseType: 'blob' })
      .then((res) => { url = URL.createObjectURL(res.data); setBarcodeSrc(url); })
      .catch(() => setBarcodeSrc(null));
    return () => url && URL.revokeObjectURL(url);
  }, [id, isNew, product.data?.barcode]);

  const fields = useMemo(() => [
    { name: 'sku', label: 'Mã SKU', required: true },
    { name: 'name', label: 'Tên sản phẩm', required: true },
    { name: 'categoryId', label: 'Nhóm hàng', type: 'select', numeric: true, options: (categories.data || []).map((c) => ({ value: c.id, label: c.name })) },
    { name: 'unitId', label: 'Đơn vị tính', type: 'select', numeric: true, options: (units.data || []).map((u) => ({ value: u.id, label: u.name })) },
    { name: 'brand', label: 'Thương hiệu' },
    { name: 'barcode', label: 'Mã vạch (bỏ trống = dùng SKU)' },
    { name: 'costPrice', label: 'Giá vốn', type: 'number' },
    { name: 'salePrice', label: 'Giá bán', type: 'number' },
    { name: 'warrantyMonths', label: 'Bảo hành (tháng)', type: 'number' },
    { name: 'serialManaged', label: 'Quản lý theo serial', type: 'checkbox' },
    { name: 'expiryManaged', label: 'Quản lý hạn sử dụng (FEFO)', type: 'checkbox' },
    { name: 'active', label: 'Đang kinh doanh', type: 'checkbox' },
    { name: 'specification', label: 'Thông số kỹ thuật', type: 'textarea' },
  ], [categories.data, units.data]);

  const save = async (values) => {
    const saved = isNew ? await ProductApi.create(values) : await ProductApi.update(id, values);
    toast.success('Đã lưu sản phẩm');
    if (isNew) navigate(`/products/${saved.id}`, { replace: true });
    else product.setData(saved);
  };

  const upload = async (file) => {
    try {
      product.setData(await ProductApi.uploadImage(id, file));
      toast.success('Đã tải ảnh lên');
    } catch (e) {
      toast.error(e);
    }
  };

  if (product.loading) return <p className="muted">Đang tải...</p>;
  if (product.error) return <div className="alert alert-error">{product.error}</div>;

  return (
    <>
      <PageHeader title={isNew ? 'Thêm sản phẩm' : `Sản phẩm ${product.data.sku}`} />
      <div className="grid-2">
        <div className="card">
          <BaseForm fields={fields} initialValues={product.data} onSubmit={save} onCancel={() => navigate('/products')} />
        </div>
        {!isNew && (
          <div className="card">
            <h2>Mã vạch</h2>
            {barcodeSrc ? <img src={barcodeSrc} alt="barcode" className="barcode" /> : <p className="muted">Không tạo được mã vạch</p>}
            <h2>Hình ảnh</h2>
            <div className="images">
              {product.data.imageUrls?.map((u) => <img key={u} src={u} alt="" />)}
            </div>
            <FileUploader accept="image/png,image/jpeg,image/webp" onUpload={upload} />
          </div>
        )}
      </div>
    </>
  );
}
