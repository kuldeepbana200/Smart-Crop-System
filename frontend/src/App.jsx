import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import OfficerDashboard from './pages/officer/OfficerDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';
import Crops from './pages/Crops';
import AddCrop from './pages/AddCrop';
import EditCrop from './pages/EditCrop';
import CropDetails from './pages/CropDetails';
import Weather from './pages/Weather';
import FutureFeatures from './pages/FutureFeatures';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster position="top-center" toastOptions={{ duration: 3000 }} />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route path="/officer" element={<OfficerDashboard />} />
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
          <Route path="/crops" element={<ProtectedRoute><Crops /></ProtectedRoute>} />
         <Route path="/crops/new" element={<ProtectedRoute><AddCrop /></ProtectedRoute>} />
         <Route path="/crops/:id" element={<ProtectedRoute><CropDetails /></ProtectedRoute>} />
         <Route path="/crops/:id/edit" element={<ProtectedRoute><EditCrop /></ProtectedRoute>} />
         <Route path="/weather" element={<ProtectedRoute><Weather /></ProtectedRoute>} />
         <Route path="/future" element={<ProtectedRoute><FutureFeatures /></ProtectedRoute>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
