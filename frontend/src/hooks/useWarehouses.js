import { useEffect, useState } from 'react';
import { InventoryApi } from '../api/InventoryApi';

let cache = null;

/** Active warehouses, fetched once per page load. */
export function useWarehouses() {
  const [warehouses, setWarehouses] = useState(cache || []);
  useEffect(() => {
    if (cache) return;
    InventoryApi.warehouses(true)
      .then((list) => {
        cache = list;
        setWarehouses(list);
      })
      .catch(() => setWarehouses([]));
  }, []);
  return warehouses;
}

export const invalidateWarehouses = () => {
  cache = null;
};
