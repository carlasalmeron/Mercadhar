import { useState } from 'react';
import useProducts from '../../hooks/useProducts';
import { productApi } from '../../api/productApi';
import ProductCard from '../../components/product/ProductCard';
import styles from './AdminProductsPage.module.css';

const AdminProductsPage = () => {
  const { products, isLoading, error, refetch } = useProducts(false);

  const handleToggle = async (id) => {
    try {
      await productApi.toggleAvailability(id);
      refetch();
    } catch {
      alert('Error al cambiar disponibilidad');
    }
  };

  if (isLoading) return <div className={styles.loading}>Cargando...</div>;
  if (error) return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.title}>Gestión de productos</h1>
        <p className={styles.subtitle}>{products.length} productos en total</p>
        <div className={styles.grid}>
          {products.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onToggleAvailability={handleToggle}
              onAddToOrder={() => {}}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default AdminProductsPage;