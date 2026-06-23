export const formatPrice = (price) =>
  `€${Number(price).toFixed(2)}`;

export const formatDate = (dateString) =>
  new Date(dateString).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

export const formatTime = (time) =>
  time?.slice(0, 5) || '';

export const translateStatus = (status) => {
  const statuses = {
    PENDING: 'Pendiente',
    CONFIRMED: 'Confirmado',
    READY: 'Listo',
    COMPLETED: 'Completado',
    CANCELLED: 'Cancelado',
  };
  return statuses[status] || status;
};