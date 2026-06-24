import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { productApi } from '../api/productApi';
import { timeSlotApi } from '../api/timeSlotApi';
import { orderApi } from '../api/orderApi';
import useProducts from '../hooks/useProducts';
import ProductCard from '../components/product/ProductCard';
import ProductList from '../components/product/ProductList';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import styles from './OrderPage.module.css';

const STEPS = ['Productos', 'Entrega', 'Confirmar'];

const OrderPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { products, isLoading } = useProducts(true);

  const [step, setStep] = useState(0);
  const [cart, setCart] = useState([]);
  const [orderType, setOrderType] = useState('PICK_UP');
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [selectedDate, setSelectedDate] = useState('');
  const [timeSlots, setTimeSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [notes, setNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');

  useEffect(() => {
      const preselected = location.state?.preselectedProduct;
      if (preselected) {
        setCart([{ product: preselected, quantity: 1 }]);
        navigate(location.pathname, { replace: true, state: {} });
      }
    }, []);

  const addToCart = (product) => {
    setCart((prev) => {
      const existing = prev.find((i) => i.product.id === product.id);
      if (existing) {
        return prev.map((i) =>
          i.product.id === product.id
            ? { ...i, quantity: i.quantity + 1 }
            : i
        );
      }
      return [...prev, { product, quantity: 1 }];
    });
  };

  const removeFromCart = (productId) => {
    setCart((prev) => prev.filter((i) => i.product.id !== productId));
  };

  const updateQuantity = (productId, quantity) => {
    if (quantity < 1) return removeFromCart(productId);
    setCart((prev) =>
      prev.map((i) =>
        i.product.id === productId ? { ...i, quantity } : i
      )
    );
  };

  const cartTotal = cart.reduce(
    (sum, i) => sum + i.product.price * i.quantity, 0
  );

  const handleNextStep = async () => {
    if (step === 0) {
      if (cart.length === 0) {
        setError('Añade al menos un producto al pedido');
        return;
      }
      setError('');
      setStep(1);
      return;
    }

    if (step === 1) {
      if (!selectedSlot) {
        setError('Selecciona una franja horaria');
        return;
      }
      if (orderType === 'DELIVERY' && !deliveryAddress.trim()) {
        setError('Introduce tu dirección de entrega');
        return;
      }
      setError('');
      setStep(2);
    }
  };

  const handleDateChange = async (date) => {
    setSelectedDate(date);
    setSelectedSlot(null);
    if (!date) return;
    try {
      const { data } = await timeSlotApi.getAvailable(date);
      setTimeSlots(data);
    } catch {
      setError('Error al cargar las franjas horarias');
    }
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    setError('');
    try {
      await orderApi.create({
        orderType,
        timeSlotId: selectedSlot.id,
        deliveryAddress: orderType === 'DELIVERY' ? deliveryAddress : null,
        notes,
        items: cart.map((i) => ({
          productId: i.product.id,
          quantity: i.quantity,
        })),
      });
      navigate('/order/success');
    } catch (err) {
      setError(
        err.response?.data?.message || 'Error al realizar el pedido'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const categories = ['all', ...new Set(products.map((p) => p.categoryName))];
  const filteredProducts = categoryFilter === 'all'
    ? products
    : products.filter((p) => p.categoryName === categoryFilter);

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.title}>Hacer un pedido</h1>

        {/* Progress bar */}
        <div className={styles.steps}>
          {STEPS.map((label, i) => (
            <div key={label} className={styles.stepItem}>
              <div className={`${styles.stepCircle}
                ${i === step ? styles.stepActive : ''}
                ${i < step ? styles.stepDone : ''}`}>
                {i < step ? '✓' : i + 1}
              </div>
              <span className={styles.stepLabel}>{label}</span>
              {i < STEPS.length - 1 && (
                <div className={`${styles.stepLine}
                  ${i < step ? styles.stepLineDone : ''}`} />
              )}
            </div>
          ))}
        </div>

        {error && <p className={styles.error}>{error}</p>}

        {step === 0 && (
          <div className={styles.stepContent}>
            <div className={styles.layout}>
              <div className={styles.productSection}>
                <div className={styles.filters}>
                  {categories.map((cat) => (
                    <button
                      key={cat}
                      className={`${styles.filterBtn}
                        ${categoryFilter === cat ? styles.filterActive : ''}`}
                      onClick={() => setCategoryFilter(cat)}
                    >
                      {cat === 'all' ? 'Todos' : cat}
                    </button>
                  ))}
                </div>

                {isLoading ? (
                  <p className={styles.loading}>Cargando productos...</p>
                ) : (
                  <ProductList
                    products={filteredProducts}
                    onAddToOrder={addToCart}
                    emptyMessage="No hay productos disponibles."
                  />
                )}
              </div>

              <div className={styles.cartSidebar}>
                <h2 className={styles.cartTitle}>Tu pedido</h2>
                {cart.length === 0 ? (
                  <p className={styles.cartEmpty}>
                    Añade productos para empezar
                  </p>
                ) : (
                  <>
                    <div className={styles.cartItems}>
                      {cart.map(({ product, quantity }) => (
                        <div key={product.id} className={styles.cartItem}>
                          <div className={styles.cartItemInfo}>
                            <span className={styles.cartItemName}>
                              {product.name}
                            </span>
                            <span className={styles.cartItemPrice}>
                              €{(product.price * quantity).toFixed(2)}
                            </span>
                          </div>
                          <div className={styles.cartItemControls}>
                            <button
                              className={styles.qtyBtn}
                              onClick={() =>
                                updateQuantity(product.id, quantity - 1)
                              }
                            >−</button>
                            <span className={styles.qty}>{quantity}</span>
                            <button
                              className={styles.qtyBtn}
                              onClick={() =>
                                updateQuantity(product.id, quantity + 1)
                              }
                            >+</button>
                          </div>
                        </div>
                      ))}
                    </div>
                    <div className={styles.cartTotal}>
                      <span>Total</span>
                      <span>€{cartTotal.toFixed(2)}</span>
                    </div>
                  </>
                )}
                <Button
                  variant="primary"
                  size="large"
                  onClick={handleNextStep}
                  disabled={cart.length === 0}
                >
                  Continuar →
                </Button>
              </div>
            </div>
          </div>
        )}

        {step === 1 && (
          <div className={styles.stepContent}>
            <div className={styles.deliveryLayout}>
              <div className={styles.deliveryOptions}>
                <h2 className={styles.sectionTitle}>Tipo de entrega</h2>
                <div className={styles.typeButtons}>
                  <button
                    className={`${styles.typeBtn}
                      ${orderType === 'PICK_UP' ? styles.typeBtnActive : ''}`}
                    onClick={() => setOrderType('PICK_UP')}
                  >
                    🏪 Recoger en tienda
                  </button>
                  <button
                    className={`${styles.typeBtn}
                      ${orderType === 'DELIVERY' ? styles.typeBtnActive : ''}`}
                    onClick={() => setOrderType('DELIVERY')}
                  >
                    🚚 Delivery a domicilio
                  </button>
                </div>

                {orderType === 'DELIVERY' && (
                  <div className={styles.addressInput}>
                    <Input
                      label="Dirección de entrega"
                      name="address"
                      value={deliveryAddress}
                      onChange={(e) => setDeliveryAddress(e.target.value)}
                      placeholder="Calle, número, ciudad..."
                      required
                    />
                  </div>
                )}

                <h2 className={styles.sectionTitle}>Franja horaria</h2>
                <Input
                  label="Selecciona una fecha"
                  name="date"
                  type="date"
                  value={selectedDate}
                  onChange={(e) => handleDateChange(e.target.value)}
                />

                {selectedDate && timeSlots.length === 0 && (
                  <p className={styles.noSlots}>
                    No hay franjas disponibles para esta fecha.
                  </p>
                )}

                {timeSlots.length > 0 && (
                  <div className={styles.slots}>
                    {timeSlots.map((slot) => (
                      <button
                        key={slot.id}
                        className={`${styles.slotBtn}
                          ${selectedSlot?.id === slot.id
                            ? styles.slotActive : ''}`}
                        onClick={() => setSelectedSlot(slot)}
                      >
                        <span className={styles.slotTime}>
                          {slot.startTime.slice(0, 5)} –{' '}
                          {slot.endTime.slice(0, 5)}
                        </span>
                        <span className={styles.slotSpots}>
                          {slot.spotsLeft} plazas
                        </span>
                      </button>
                    ))}
                  </div>
                )}

                <div className={styles.notesInput}>
                  <Input
                    label="Notas adicionales (opcional)"
                    name="notes"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Instrucciones especiales..."
                  />
                </div>
              </div>

              <div className={styles.cartSidebar}>
                <h2 className={styles.cartTitle}>Resumen</h2>
                <div className={styles.cartItems}>
                  {cart.map(({ product, quantity }) => (
                    <div key={product.id} className={styles.cartItem}>
                      <span className={styles.cartItemName}>
                        {product.name} x{quantity}
                      </span>
                      <span className={styles.cartItemPrice}>
                        €{(product.price * quantity).toFixed(2)}
                      </span>
                    </div>
                  ))}
                </div>
                <div className={styles.cartTotal}>
                  <span>Total</span>
                  <span>€{cartTotal.toFixed(2)}</span>
                </div>
                <div className={styles.navButtons}>
                  <Button variant="outline" onClick={() => setStep(0)}>
                    ← Volver
                  </Button>
                  <Button variant="primary" onClick={handleNextStep}>
                    Confirmar →
                  </Button>
                </div>
              </div>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className={styles.stepContent}>
            <div className={styles.confirmLayout}>
              <div className={styles.confirmCard}>
                <h2 className={styles.sectionTitle}>Resumen del pedido</h2>

                <div className={styles.confirmSection}>
                  <h3>Productos</h3>
                  {cart.map(({ product, quantity }) => (
                    <div key={product.id} className={styles.confirmItem}>
                      <span>{product.name} x{quantity}</span>
                      <span>€{(product.price * quantity).toFixed(2)}</span>
                    </div>
                  ))}
                  <div className={styles.confirmTotal}>
                    <strong>Total</strong>
                    <strong>€{cartTotal.toFixed(2)}</strong>
                  </div>
                </div>

                <div className={styles.confirmSection}>
                  <h3>Entrega</h3>
                  <p>
                    {orderType === 'PICK_UP'
                      ? '🏪 Recogida en tienda'
                      : `🚚 Delivery a: ${deliveryAddress}`}
                  </p>
                  <p>
                    📅 {selectedSlot?.date} —{' '}
                    {selectedSlot?.startTime?.slice(0, 5)} a{' '}
                    {selectedSlot?.endTime?.slice(0, 5)}
                  </p>
                  {notes && <p>📝 {notes}</p>}
                </div>

                <div className={styles.confirmSection}>
                  <h3>Pago</h3>
                  <p>💵 Efectivo al {orderType === 'PICK_UP'
                    ? 'recoger' : 'recibir'}</p>
                </div>

                <div className={styles.navButtons}>
                  <Button variant="outline" onClick={() => setStep(1)}>
                    ← Volver
                  </Button>
                  <Button
                    variant="primary"
                    size="large"
                    onClick={handleSubmit}
                    isLoading={isSubmitting}
                  >
                    ✅ Confirmar pedido
                  </Button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderPage;