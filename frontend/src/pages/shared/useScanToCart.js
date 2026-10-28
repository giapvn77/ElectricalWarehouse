import { useCallback } from 'react';
import { useDispatch } from 'react-redux';
import { ProductApi } from '../../api/ProductApi';
import { useToast } from '../../hooks/useToast';
import { addItem } from '../../store/CartStore';

/** Returns handlers that add a product to a cart, either picked from search or scanned by barcode. */
export function useScanToCart(cart, priceField) {
  const dispatch = useDispatch();
  const toast = useToast();

  const addProduct = useCallback((p) => {
    dispatch(addItem({ cart, item: {
      productId: p.id, sku: p.sku, name: p.name, serialManaged: p.serialManaged, expiryManaged: p.expiryManaged,
      unitPrice: p[priceField] ?? 0, quantity: 1,
    } }));
  }, [dispatch, cart, priceField]);

  const scan = useCallback(async (code) => {
    try {
      addProduct(await ProductApi.lookup(code));
    } catch {
      toast.error('Không tìm thấy sản phẩm có mã ' + code);
    }
  }, [addProduct, toast]);

  return { addProduct, scan };
}
