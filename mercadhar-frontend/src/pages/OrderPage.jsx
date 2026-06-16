import styles from './OrderPage.module.css';

const OrderPage = () => {
  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.title}>Hacer un pedido</h1>
        <p className={styles.subtitle}>Selecciona tus productos y una franja horaria.</p>
      </div>
    </div>
  );
};

export default OrderPage;