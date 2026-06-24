import { useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../api/orderApi';
import { formatDate, formatTime } from '../utils/formatters';
import useOrders from '../hooks/useOrders';
import Button from '../components/ui/Button';
import styles from './MyOrdersPage.module.css';

const CANCELLABLE = ['PENDING', 'CONFIRMED'];

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'READY', 'COMPLETED'];

const STATUS_META = {
  PENDING:   { label: 'Pendiente',   icon: '🕐', desc: 'Hemos recibido tu pedido y lo estamos revisando.' },
  CONFIRMED: { label: 'Confirmado',  icon: '✅', desc: 'Tu pedido está confirmado y en preparación.' },
  READY:     { label: 'Listo',       icon: '📦', desc: 'Tu pedido está listo para recoger o en camino.' },
  COMPLETED: { label: 'Completado',  icon: '🎉', desc: '¡Pedido entregado! Gracias por confiar en Mercadhar.' },
  CANCELLED: { label: 'Cancelado',   icon: '❌', desc: 'Este pedido fue cancelado.' },
};

const StatusTracker = ({ status }) => {
  if (status === 'CANCELLED') {
    return (
      <div className={styles.cancelled}>
        <span>❌</span> Pedido cancelado
      </div>
    );
  }

  const currentIndex = STATUS_STEPS.indexOf(status);

  return (
    <div className={styles.tracker}>
      {STATUS_STEPS.map((step, i) => {
        const meta = STATUS_META[step];
        const done = i < currentIndex;
        const active = i === currentIndex;
        return (
          <div key={step} className={styles.trackerStep}>
            {i > 0 && (
              <div className={`${styles.line} ${done || active ? styles.lineDone : ''}`} />
            )}
            <div className={`${styles.stepDot}
              ${done ? styles.dotDone : ''}
              ${active ? styles.dotActive : ''}
            `}>
              {done ? '✓' : meta.icon}
            </div>
            <span className={`${styles.stepLabel}
              ${active ? styles.stepLabelActive : ''}
              ${done ? styles.stepLabelDone : ''}
            `}>
              {meta.label}
            </span>
          </div>
        );
      })}
    </div>
  );
};

const MyOrdersPage = () => {
  const { orders, isLoading, error, refetch } = useOrders('mine');
  const [cancellingId, setCancellingId] = useState(null);
  const [expandedId, setExpandedId] = useState(null);

  const handleCancel = async (orderId) => {
    if (!window.confirm('¿Seguro que quieres cancelar este pedido?')) return;
    setCancellingId(orderId);
    try {
      await orderApi.cancel(orderId);
      refetch();
    } catch {
      alert('No se pudo cancelar el pedido. Inténtalo de nuevo.');
    } finally {
      setCancellingId(null);
    }
  };

  if (isLoading) return <div className={styles.loading}>Cargando tus pedidos...</div>;
  if (error)     return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h1 className={styles.title}>Mis pedidos</h1>
          <Link to="/order">
            <Button variant="primary" size="small">+ Nuevo pedido</Button>
          </Link>
        </div>

        {orders.length === 0 ? (
          <div className={styles.empty}>
            <span className={styles.emptyIcon}>🛒</span>
            <p>Todavía no tienes pedidos.</p>
            <Link to="/order">
              <Button variant="primary">Hacer mi primer pedido</Button>
            </Link>
          </div>
        ) : (
          <div className={styles.list}>
            {orders.map((order) => {
              const isExpanded = expandedId === order.id;
              const meta = STATUS_META[order.status] ?? STATUS_META.PENDING;

              return (
                <div
                  key={order.id}
                  className={`${styles.card} ${order.status === 'CANCELLED' ? styles.cardCancelled : ''}`}
                >

                  <div
                    className={styles.cardHeader}
                    onClick={() => setExpandedId(isExpanded ? null : order.id)}
                  >
                    <div className={styles.headerLeft}>
                      <span className={styles.orderId}>Pedido #{order.id}</span>
                      <span className={styles.orderDate}>
                        {formatDate(order.createdAt)}
                      </span>
                    </div>
                    <div className={styles.headerRight}>
                      <span className={`${styles.badge} ${styles[order.status.toLowerCase()]}`}>
                        {meta.icon} {meta.label}
                      </span>
                      <span className={styles.total}>
                        €{Number(order.totalAmount).toFixed(2)}
                      </span>
                      <span className={styles.chevron}>
                        {isExpanded ? '▲' : '▼'}
                      </span>
                    </div>
                  </div>

                  <div className={styles.trackerWrapper}>
                    <StatusTracker status={order.status} />
                    <p className={styles.statusDesc}>{meta.desc}</p>
                  </div>

                  {isExpanded && (
                    <div className={styles.cardBody}>
                      <div className={styles.section}>
                        <h4 className={styles.sectionTitle}>Productos</h4>
                        {order.items.map((item, i) => (
                          <div key={i} className={styles.item}>
                            <span>{item.productName} × {item.quantity}</span>
                            <span>€{Number(item.subtotal).toFixed(2)}</span>
                          </div>
                        ))}
                        <div className={styles.itemTotal}>
                          <strong>Total</strong>
                          <strong>€{Number(order.totalAmount).toFixed(2)}</strong>
                        </div>
                      </div>

                      <div className={styles.section}>
                        <h4 className={styles.sectionTitle}>Entrega</h4>
                        <p>
                          {order.orderType === 'DELIVERY'
                            ? `🚚 Delivery — ${order.deliveryAddress}`
                            : '🏪 Recogida en tienda'}
                        </p>
                        <p>
                          📅 {order.timeSlotDate} · {formatTime(order.timeSlotStart)} – {formatTime(order.timeSlotEnd)}
                        </p>
                        {order.notes && <p>📝 {order.notes}</p>}
                      </div>

                      {CANCELLABLE.includes(order.status) && (
                        <div className={styles.actions}>
                          <Button
                            variant="danger"
                            size="small"
                            isLoading={cancellingId === order.id}
                            onClick={() => handleCancel(order.id)}
                          >
                            Cancelar pedido
                          </Button>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyOrdersPage;