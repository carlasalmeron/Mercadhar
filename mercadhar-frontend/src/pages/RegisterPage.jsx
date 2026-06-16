import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import styles from './RegisterPage.module.css';

const INITIAL_FORM = {
  name: '',
  email: '',
  password: '',
  phone: '',
  address: '',
};

const RegisterPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState(INITIAL_FORM);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const { data } = await authApi.register(formData);
      login({ name: data.name, email: data.email, role: data.role }, data.token);
      navigate('/catalog');
    } catch (err) {
      setError(
        err.response?.data?.message || 'Error al registrarse. Inténtalo de nuevo.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>Crear cuenta</h1>
        <p className={styles.subtitle}>Únete a Mercadhar 🛒</p>

        {error && <p className={styles.error}>{error}</p>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <Input label="Nombre" name="name" value={formData.name}
            onChange={handleChange} placeholder="Tu nombre" required />
          <Input label="Email" name="email" type="email"
            value={formData.email} onChange={handleChange}
            placeholder="tu@email.com" required />
          <Input label="Contraseña" name="password" type="password"
            value={formData.password} onChange={handleChange}
            placeholder="Mínimo 6 caracteres" required />
          <Input label="Teléfono" name="phone" value={formData.phone}
            onChange={handleChange} placeholder="+34 600 000 000" required />
          <Input label="Dirección" name="address" value={formData.address}
            onChange={handleChange} placeholder="Tu dirección" required />

          <Button type="submit" variant="primary" size="large" isLoading={isLoading}>
            Crear cuenta
          </Button>
        </form>

        <p className={styles.footer}>
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className={styles.link}>Inicia sesión</Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;