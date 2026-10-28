import { useCallback, useEffect, useState } from 'react';
import { getErrorMessage } from '../utils/format';

/**
 * Loads a PagedResult from fetcher({ ...filters, page, size }) and reloads when filters change.
 * filters must be a stable value (useState / useMemo).
 */
export function usePagedQuery(fetcher, filters, size = 20) {
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], page: 0, size, totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await fetcher({ ...filters, page, size }));
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [fetcher, filters, page, size]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [filters]);

  return { data, loading, error, page, setPage, reload: load };
}
