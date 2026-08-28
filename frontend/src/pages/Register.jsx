import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'motion/react';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
  try {
  await register(form);
  toast.success('Registration successful — please log in');
  navigate('/login');
} catch (err) {
  toast.error('Registration failed. Check your details and try again.');
} finally {
      setLoading(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="min-h-screen flex items-center justify-center bg-green-50 px-4"
    >
      <form onSubmit={handleSubmit} className="bg-white shadow-md rounded-xl p-8 w-full max-w-sm">
        <h1 className="text-2xl font-bold text-green-700 mb-6">Farmer Registration</h1>
        {error && <p className="text-red-600 text-sm mb-4">{error}</p>}
        {['name', 'email', 'phone', 'password'].map((field) => (
          <input
            key={field}
            type={field === 'password' ? 'password' : 'text'}
            name={field}
            placeholder={field[0].toUpperCase() + field.slice(1)}
            value={form[field]}
            onChange={handleChange}
            className="w-full border rounded-lg px-3 py-2 mb-4"
            required
          />
        ))}
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-green-600 text-white rounded-lg py-2 font-semibold hover:bg-green-700 disabled:opacity-50"
        >
          {loading ? 'Registering...' : 'Register'}
        </button>
        <p className="text-sm text-center mt-4">
          Have an account? <Link to="/login" className="text-green-700 font-medium">Login</Link>
        </p>
      </form>
    </motion.div>
  );
}