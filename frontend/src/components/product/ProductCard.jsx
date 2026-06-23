import styles from './ProductCard.module.css';
import Button from '../ui/Button';
import useAuth from '../../hooks/useAuth';

const ProductCard = ({ product, onAddToOrder, onToggleAvailability }) => {
  const { isAdmin } = useAuth();

  return (
    <div className={`${styles.card} ${!product.available ? styles.unavailable : ''}`}>
      <div className={styles.imageWrapper}>
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className={styles.image}
          />
        ) : (
          <div className={styles.imagePlaceholder}>🛒</div>
        )}
        {!product.available && (
          <span className={styles.badge}>No disponible</span>
        )}
      </div>

      <div className={styles.content}>
        <span className={styles.category}>{product.categoryName}</span>
        <h3 className={styles.name}>{product.name}</h3>
        {product.description && (
          <p className={styles.description}>{product.description}</p>
        )}
        <p className={styles.price}>€{product.price.toFixed(2)}</p>
      </div>

      <div className={styles.actions}>
        {isAdmin() ? (
          <Button
            variant={product.available ? 'danger' : 'secondary'}
            size="small"
            onClick={() => onToggleAvailability(product.id)}
          >
            {product.available ? 'Desactivar' : 'Activar'}
          </Button>
        ) : (
          product.available && (
            <Button
              variant="primary"
              size="small"
              onClick={() => onAddToOrder(product)}
            >
              Añadir al pedido
            </Button>
          )
        )}
      </div>
    </div>
  );
};

export default ProductCard;