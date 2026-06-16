import { Link } from 'react-router-dom';
import Button from '../components/ui/Button';
import styles from './HomePage.module.css';

const HomePage = () => {
  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <h1 className={styles.heroTitle}>
            El sabor de Venezuela,<br />cerca de ti 🇻🇪
          </h1>
          <p className={styles.heroSubtitle}>
            Productos venezolanos auténticos. Pide online y recoge o recibe en tu casa.
          </p>
          <div className={styles.heroButtons}>
            <Link to="/catalog">
              <Button variant="primary" size="large">Ver catálogo</Button>
            </Link>
            <Link to="/order">
              <Button variant="secondary" size="large">Hacer pedido</Button>
            </Link>
          </div>
        </div>
      </section>

      <section className={styles.features}>
        <div className={styles.featuresGrid}>
          {[
            { icon: '🏪', title: 'Pickup', desc: 'Recoge en tienda en la franja que prefieras' },
            { icon: '🚚', title: 'Delivery', desc: 'Te lo llevamos a tu puerta' },
            { icon: '💵', title: 'Pago en efectivo', desc: 'Solo efectivo, sin complicaciones' },
          ].map(({ icon, title, desc }) => (
            <div key={title} className={styles.featureCard}>
              <span className={styles.featureIcon}>{icon}</span>
              <h3 className={styles.featureTitle}>{title}</h3>
              <p className={styles.featureDesc}>{desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default HomePage;