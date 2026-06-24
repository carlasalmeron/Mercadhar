import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useProducts from '../hooks/useProducts';
import useAuth from '../hooks/useAuth';
import { productApi } from '../api/productApi';
import ProductCard from '../components/product/ProductCard';
import ProductList from '../components/product/ProductList';
import styles from './CatalogPage.module.css';

const CatalogPage = () => {
  const navigate = useNavigate();
  const { isAdmin, isAuthenticated } = useAuth();
  const { products, isLoading, error, refetch } = useProducts(!isAdmin());
  const [categoryFilter, setCategoryFilter] = useState('all');

  const categories = ['all', ...new Set(products.map((p) => p.categoryName))];

  const filteredProducts = categoryFilter === 'all'
    ? products
    : products.filter((p) => p.categoryName === categoryFilter);

  const handleToggleAvailability = async (productId) => {
    try {
      await productApi.toggleAvailability(productId);
      refetch();
    } catch {
      alert('Error al cambiar disponibilidad');
    }
  };

  const handleAddToOrder = (product) => {
    if (!isAuthenticated()) {
      navigate('/login');
      return;
    }
    navigate('/order', { state: { preselectedProduct: product } });
  };

  if (isLoading) return <div className={styles.loading}>Cargando productos...</div>;
  if (error) return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.title}>Catálogo de productos</h1>
        <p className={styles.subtitle}>Productos venezolanos de calidad 🇻🇪</p>

        <div className={styles.filters}>
          {categories.map((cat) => (
            <button
              key={cat}
              className={`${styles.filterBtn} ${
                  categoryFilter === cat ? styles.active : ''
              }`}
              onClick={() => setCategoryFilter(cat)}
            >
              {cat === 'all' ? 'Todos' : cat}
            </button>
          ))}
        </div>

        <ProductList
          products={filteredProducts}
          onAddToOrder={handleAddToOrder}
          onToggleAvailability={handleToggleAvailability}
          emptyMessage="No hay productos disponibles en esta categoría."
        />
      </div>
    </div>
  );
};

export default CatalogPage;