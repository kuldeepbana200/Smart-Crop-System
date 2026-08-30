import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const ProtectedRoute = ({ element, skipProfileCheck = false }) => {
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return <div className="flex h-[100vh] items-center justify-center">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
    </div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!skipProfileCheck && !user?.phoneVerified) {
    return <Navigate to="/verify-otp" replace />;
  }

  return element;
};

export default ProtectedRoute;