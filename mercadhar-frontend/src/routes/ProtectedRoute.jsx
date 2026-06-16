import { Navigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { isAuthenticated, isAdmin, isLoading } = useAuth();

  if (isLoading) return <div>Cargando...</div>;

  if (!isAuthenticated()) return <Navigate to="/login" replace />;

  if (requireAdmin && !isAdmin()) return <Navigate to="/" replace />;

  return children;
};

export default ProtectedRoute;