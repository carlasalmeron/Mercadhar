import { Link } from 'react-router-dom';
import styles from './Footer.module.css';

const Footer = () => (
  <footer className={styles.footer}>
    <div className={styles.container}>
      <div className={styles.brand}>
        <span className={styles.logo}>🛒 Mercadhar</span>
        <p className={styles.tagline}>
          El sabor de Venezuela, cerca de ti 🇻🇪
        </p>
      </div>

      <div className={styles.links}>
        <div className={styles.column}>
          <h4 className={styles.columnTitle}>Tienda</h4>
          <Link to="/catalog" className={styles.link}>Catálogo</Link>
          <Link to="/order" className={styles.link}>Hacer pedido</Link>
          <Link to="/my-orders" className={styles.link}>Mis pedidos</Link>
        </div>
        <div className={styles.column}>
          <h4 className={styles.columnTitle}>Cuenta</h4>
          <Link to="/login" className={styles.link}>Iniciar sesión</Link>
          <Link to="/register" className={styles.link}>Registrarse</Link>
        </div>
      </div>
    </div>

    <div className={styles.bottom}>
      <p>© {new Date().getFullYear()} Mercadhar · Pago en efectivo</p>
    </div>
  </footer>
);

export default Footer;