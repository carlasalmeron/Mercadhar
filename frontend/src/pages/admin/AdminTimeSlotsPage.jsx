import { useState, useEffect } from 'react';
import { timeSlotApi } from '../../api/timeSlotApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Modal from '../../components/ui/Modal';
import { formatTime } from '../../utils/formatters';
import styles from './AdminTimeSlotsPage.module.css';

const EMPTY_FORM = {
  date: '',
  startTime: '',
  endTime: '',
  maxOrders: 10,
};

const AdminTimeSlotsPage = () => {
  const [slots, setSlots] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [filterDate, setFilterDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const fetchSlots = async (date) => {
    if (!date) return;
    setIsLoading(true);
    setError('');
    try {
      const { data } = await timeSlotApi.getAll(date);
      setSlots(data);
    } catch {
      setError('Error al cargar las franjas horarias');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { fetchSlots(filterDate); }, [filterDate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    if (!formData.date) return 'La fecha es obligatoria';
    if (!formData.startTime) return 'La hora de inicio es obligatoria';
    if (!formData.endTime) return 'La hora de fin es obligatoria';
    if (formData.startTime >= formData.endTime)
      return 'La hora de fin debe ser posterior a la de inicio';
    if (!formData.maxOrders || Number(formData.maxOrders) < 1)
      return 'El máximo de pedidos debe ser al menos 1';
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationError = validate();
    if (validationError) { setFormError(validationError); return; }

    setIsSaving(true);
    setFormError('');
    try {
      await timeSlotApi.create({
        ...formData,
        maxOrders: parseInt(formData.maxOrders),
      });
      setIsModalOpen(false);
      setFormData(EMPTY_FORM);
      fetchSlots(filterDate);
    } catch (err) {
      setFormError(
        err.response?.data?.message || 'Error al crear la franja horaria'
      );
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Eliminar esta franja horaria?')) return;
    setDeletingId(id);
    try {
      await timeSlotApi.delete(id);
      fetchSlots(filterDate);
    } catch {
      alert('No se pudo eliminar. Puede que tenga pedidos asociados.');
    } finally {
      setDeletingId(null);
    }
  };

  const openModal = () => {
    setFormData({ ...EMPTY_FORM, date: filterDate });
    setFormError('');
    setIsModalOpen(true);
  };

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>Franjas horarias</h1>
            <p className={styles.subtitle}>
              Gestiona los horarios de recogida y entrega
            </p>
          </div>
          <Button variant="primary" onClick={openModal}>
            + Nueva franja
          </Button>
        </div>

        {/* Date filter */}
        <div className={styles.filterBar}>
          <label className={styles.filterLabel}>Ver franjas del día:</label>
          <input
            type="date"
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
            className={styles.dateInput}
          />
        </div>

        {isLoading && <p className={styles.loading}>Cargando...</p>}
        {error && <p className={styles.error}>{error}</p>}

        {!isLoading && slots.length === 0 && (
          <div className={styles.empty}>
            <span className={styles.emptyIcon}>🕐</span>
            <p>No hay franjas para esta fecha.</p>
            <Button variant="primary" onClick={openModal}>
              Crear la primera
            </Button>
          </div>
        )}

        {slots.length > 0 && (
          <div className={styles.grid}>
            {slots.map((slot) => {
              const occupancy = slot.currentOrders ?? 0;
              const pct = Math.min(
                Math.round((occupancy / slot.maxOrders) * 100),
                100
              );
              const isFull = occupancy >= slot.maxOrders;

              return (
                <div
                  key={slot.id}
                  className={`${styles.card} ${isFull ? styles.cardFull : ''}`}
                >
                  <div className={styles.cardTop}>
                    <span className={styles.time}>
                      {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                    </span>
                    {isFull && (
                      <span className={styles.fullBadge}>Completa</span>
                    )}
                  </div>

                  <div className={styles.occupancy}>
                    <div className={styles.occupancyBar}>
                      <div
                        className={styles.occupancyFill}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                    <span className={styles.occupancyLabel}>
                      {occupancy} / {slot.maxOrders} pedidos
                    </span>
                  </div>

                  <button
                    className={styles.deleteBtn}
                    onClick={() => handleDelete(slot.id)}
                    disabled={deletingId === slot.id || occupancy > 0}
                    title={
                      occupancy > 0
                        ? 'No se puede eliminar: tiene pedidos'
                        : 'Eliminar franja'
                    }
                  >
                    {deletingId === slot.id ? '...' : '🗑️'}
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Nueva franja horaria"
      >
        <form onSubmit={handleSubmit} className={styles.form}>
          {formError && <p className={styles.formError}>{formError}</p>}

          <Input
            label="Fecha"
            name="date"
            type="date"
            value={formData.date}
            onChange={handleChange}
            required
          />
          <div className={styles.timeRow}>
            <Input
              label="Hora inicio"
              name="startTime"
              type="time"
              value={formData.startTime}
              onChange={handleChange}
              required
            />
            <Input
              label="Hora fin"
              name="endTime"
              type="time"
              value={formData.endTime}
              onChange={handleChange}
              required
            />
          </div>
          <Input
            label="Máximo de pedidos"
            name="maxOrders"
            type="number"
            value={formData.maxOrders}
            onChange={handleChange}
            required
          />

          <div className={styles.formActions}>
            <Button
              variant="outline"
              type="button"
              onClick={() => setIsModalOpen(false)}
            >
              Cancelar
            </Button>
            <Button variant="primary" type="submit" isLoading={isSaving}>
              Crear franja
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default AdminTimeSlotsPage;
