import ProductCard from './ProductCard';
import styles from './ProductList.module.css';

const ProductList = ({
  products = [],
  onAddToOrder = () => {},
  onToggleAvailability = () => {},
  emptyMessage = 'No hay productos disponibles.',
}) => {
  if (products.length === 0) {
    return <p className={styles.empty}>{emptyMessage}</p>;
  }

  return (
    <div className={styles.grid}>
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          onAddToOrder={onAddToOrder}
          onToggleAvailability={onToggleAvailability}
        />
      ))}
    </div>
  );
};

export default ProductList;