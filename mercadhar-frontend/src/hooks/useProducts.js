import { useState, useEffect } from 'react';
import { productApi } from '../api/productApi';

const useProducts = (onlyAvailable = true) => {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProducts = async () => {
    try {
      setIsLoading(true);
      const { data } = onlyAvailable
        ? await productApi.getAvailable()
        : await productApi.getAll();
      setProducts(data);
    } catch (err) {
      setError('Error loading products');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [onlyAvailable]);

  return { products, isLoading, error, refetch: fetchProducts };
};

export default useProducts;