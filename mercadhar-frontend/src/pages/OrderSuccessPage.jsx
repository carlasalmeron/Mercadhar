import { Link } from 'react-router-dom';
import Button from '../components/ui/Button';
import styles from './OrderSuccessPage.module.css';

const OrderSuccessPage = () => (
  <div className={styles.page}>
    <div className={styles.card}>
      <div className={styles.icon}>✅</div>
      <h1 className={styles.title}>¡Pedido confirmado!</h1>
      <p className={styles.subtitle}>
        Tu pedido ha sido recibido. Te esperamos en la franja horaria seleccionada.
        El pago es en efectivo.
      </p>
      <div className={styles.buttons}>
        <Link to="/catalog">
          <Button variant="outline">Ver catálogo</Button>
        </Link>
        <Link to="/">
          <Button variant="primary">Ir al inicio</Button>
        </Link>
      </div>
    </div>
  </div>
);

export default OrderSuccessPage;