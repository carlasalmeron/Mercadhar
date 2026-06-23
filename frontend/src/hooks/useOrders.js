import { useState, useEffect, useCallback } from 'react';
import { orderApi } from '../api/orderApi';

const useOrders = (scope = 'mine') => {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchOrders = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const { data } = scope === 'all'
        ? await orderApi.getAll()
        : await orderApi.getMyOrders();

      const sorted = [...data].sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      );
      setOrders(sorted);
    } catch {
      setError(
        scope === 'all'
          ? 'Error al cargar los pedidos'
          : 'Error al cargar tus pedidos'
      );
    } finally {
      setIsLoading(false);
    }
  }, [scope]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  return { orders, isLoading, error, refetch: fetchOrders };
};

export default useOrders;
