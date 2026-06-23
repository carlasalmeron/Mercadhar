import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import styles from './Navbar.module.css';
import Button from '../ui/Button';

const Navbar = () => {
  const { user, logout, isAdmin, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className={styles.navbar}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>
          🛒 Mercadhar
        </Link>

        <div className={styles.links}>
          <Link to="/catalog" className={styles.link}>Catálogo</Link>

          {isAuthenticated() && !isAdmin() && (
            <Link to="/order" className={styles.link}>Hacer pedido</Link>
          )}

          {isAdmin() && (
            <>
              <Link to="/admin/products" className={styles.link}>Productos</Link>
              <Link to="/admin/orders" className={styles.link}>Pedidos</Link>
              <Link to="/admin/timeslots" className={styles.link}>Franjas</Link>
            </>
          )}
        </div>

        <div className={styles.auth}>
          {isAuthenticated() ? (
            <div className={styles.userMenu}>
              <span className={styles.userName}>
                {isAdmin() ? '👑' : '👤'} {user.name}
              </span>
              <Button variant="outline" size="small" onClick={handleLogout}>
                Salir
              </Button>
            </div>
          ) : (
            <div className={styles.authButtons}>
              <Link to="/login">
                <Button variant="outline" size="small">Entrar</Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="small">Registrarse</Button>
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;