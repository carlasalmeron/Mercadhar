import { useState, useEffect } from 'react';
import { orderApi } from '../../api/orderApi';
import { formatDate, formatTime, translateStatus } from '../../utils/formatters';
import styles from './AdminOrdersPage.module.css';

const STATUS_OPTIONS = ['PENDING', 'CONFIRMED', 'READY', 'COMPLETED', 'CANCELLED'];
const STATUS_FILTERS = ['ALL', ...STATUS_OPTIONS];
const PAGE_SIZE = 10;

const AdminOrdersPage = () => {
  const [allOrders, setAllOrders]   = useState([]);   // todos los pedidos en memoria
  const [isLoading, setIsLoading]   = useState(true);
  const [error, setError]           = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [currentPage, setCurrentPage]   = useState(1);

  const fetchOrders = async () => {
    setIsLoading(true);
    try {
      const { data } = await orderApi.getAll();
      // Más reciente primero
      const sorted = [...data].sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      );
      setAllOrders(sorted);
    } catch {
      setError('Error al cargar los pedidos');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { fetchOrders(); }, []);

  // Volver a página 1 cuando cambia el filtro
  useEffect(() => { setCurrentPage(1); }, [statusFilter]);

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      await orderApi.updateStatus(orderId, newStatus);
      // Actualiza solo el pedido afectado en memoria (sin refetch completo)
      setAllOrders((prev) =>
        prev.map((o) => o.id === orderId ? { ...o, status: newStatus } : o)
      );
    } catch {
      alert('Error al actualizar el estado');
    }
  };

  // ── Filtrado y paginación (solo en frontend) ──────────────
  const filtered = statusFilter === 'ALL'
    ? allOrders
    : allOrders.filter((o) => o.status === statusFilter);

  const totalPages  = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage    = Math.min(currentPage, totalPages);
  const pageOrders  = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  if (isLoading) return <div className={styles.loading}>Cargando pedidos...</div>;
  if (error)     return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.page}>
      <div className={styles.container}>

        {/* Cabecera */}
        <div className={styles.header}>
          <h1 className={styles.title}>Gestión de pedidos</h1>
          <span className={styles.count}>{filtered.length} pedido{filtered.length !== 1 ? 's' : ''}</span>
        </div>

        {/* Filtros de estado */}
        <div className={styles.filters}>
          {STATUS_FILTERS.map((s) => (
            <button
              key={s}
              className={`${styles.filterBtn} ${statusFilter === s ? styles.filterActive : ''}`}
              onClick={() => setStatusFilter(s)}
            >
              {s === 'ALL' ? 'Todos' : translateStatus(s)}
              <span className={styles.filterCount}>
                {s === 'ALL'
                  ? allOrders.length
                  : allOrders.filter((o) => o.status === s).length}
              </span>
            </button>
          ))}
        </div>

        {/* Lista de pedidos */}
        {pageOrders.length === 0 ? (
          <p className={styles.empty}>No hay pedidos con este estado.</p>
        ) : (
          <div className={styles.list}>
            {pageOrders.map((order) => (
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
                  <p><strong>Franja:</strong> {order.timeSlotDate} · {formatTime(order.timeSlotStart)} – {formatTime(order.timeSlotEnd)}</p>
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

        {/* Paginación */}
        {totalPages > 1 && (
          <div className={styles.pagination}>
            <button
              className={styles.pageBtn}
              onClick={() => setCurrentPage(1)}
              disabled={safePage === 1}
            >«</button>
            <button
              className={styles.pageBtn}
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              disabled={safePage === 1}
            >‹</button>

            {Array.from({ length: totalPages }, (_, i) => i + 1)
              .filter((p) => Math.abs(p - safePage) <= 2)
              .map((p) => (
                <button
                  key={p}
                  className={`${styles.pageBtn} ${p === safePage ? styles.pageBtnActive : ''}`}
                  onClick={() => setCurrentPage(p)}
                >{p}</button>
              ))}

            <button
              className={styles.pageBtn}
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              disabled={safePage === totalPages}
            >›</button>
            <button
              className={styles.pageBtn}
              onClick={() => setCurrentPage(totalPages)}
              disabled={safePage === totalPages}
            >»</button>

            <span className={styles.pageInfo}>
              Página {safePage} de {totalPages}
            </span>
          </div>
        )}

      </div>
    </div>
  );
};

export default AdminOrdersPage;