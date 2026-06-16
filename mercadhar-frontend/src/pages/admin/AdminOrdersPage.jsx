import { useState, useEffect } from 'react';
import { orderApi } from '../../api/orderApi';
import { formatDate, formatTime, translateStatus } from '../../utils/formatters';
import Button from '../../components/ui/Button';
import styles from './AdminOrdersPage.module.css';

const STATUS_OPTIONS = ['PENDING', 'CONFIRMED', 'READY', 'COMPLETED', 'CANCELLED'];

const AdminOrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchOrders = async () => {
    try {
      const { data } = await orderApi.getAll();
      setOrders(data);
    } catch {
      setError('Error al cargar los pedidos');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      await orderApi.updateStatus(orderId, newStatus);
      fetchOrders();
    } catch {
      alert('Error al actualizar el estado');
    }
  };

  if (isLoading) return <div className={styles.loading}>Cargando pedidos...</div>;
  if (error) return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.title}>Gestión de pedidos</h1>

        {orders.length === 0 ? (
          <p className={styles.empty}>No hay pedidos todavía.</p>
        ) : (
          <div className={styles.list}>
            {orders.map((order) => (
              <div key={order.id} className={styles.card}>
                <div className={styles.cardHeader}>
                  <span className={styles.orderId}>Pedido #{order.id}</span>
                  <span className={`${styles.badge} ${styles[order.status.toLowerCase()]}`}>
                    {translateStatus(order.status)}
                  </span>
                </div>

                <div className={styles.cardBody}>
                  <p><strong>Cliente:</strong> {order.customerName}</p>
                  <p><strong>Tipo:</strong> {order.orderType === 'DELIVERY' ? '🚚 Delivery' : '🏪 Pickup'}</p>
                  <p><strong>Franja:</strong> {order.timeSlotDate} {formatTime(order.timeSlotStart)} - {formatTime(order.timeSlotEnd)}</p>
                  {order.deliveryAddress && <p><strong>Dirección:</strong> {order.deliveryAddress}</p>}
                  <p><strong>Total:</strong> €{Number(order.totalAmount).toFixed(2)}</p>
                  <p><strong>Fecha:</strong> {formatDate(order.createdAt)}</p>
                </div>

                <div className={styles.cardFooter}>
                  <select
                    value={order.status}
                    onChange={(e) => handleStatusChange(order.id, e.target.value)}
                    className={styles.select}
                  >
                    {STATUS_OPTIONS.map((s) => (
                      <option key={s} value={s}>{translateStatus(s)}</option>
                    ))}
                  </select>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminOrdersPage;