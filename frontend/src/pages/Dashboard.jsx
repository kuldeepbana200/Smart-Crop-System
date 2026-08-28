import { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getCurrentFarmer } from '../services/farmerService';
import ProfileSetup from './ProfileSetup';
import toast from 'react-hot-toast';

export default function Dashboard() {
  const { logout } = useAuth();
  const [farmer, setFarmer] = useState(null);
  const [status, setStatus] = useState('loading'); // loading | ready | needsProfile | error

  useEffect(() => {
    getCurrentFarmer()
      .then((data) => {
        setFarmer(data);
        setStatus('ready');
      })
      .catch((err) => {
        if (err.response?.status === 404) {
          setStatus('needsProfile');
        } else {
          setStatus('error');
          toast.error('Could not load your dashboard. Please try again.');
        }
      });
  }, []);

  if (status === 'loading') {
    return <div className="min-h-screen flex items-center justify-center text-gray-500">Loading your dashboard...</div>;
  }

  if (status === 'needsProfile') {
    return <ProfileSetup />;
  }

  if (status === 'error') {
    return (
      <div className="min-h-screen flex items-center justify-center text-red-600">
        Something went wrong loading your profile. Please try again later.
      </div>
    );
  }

  return (
    <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="min-h-screen bg-gray-50 p-8">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-green-700">Welcome, {farmer.name}</h1>
          <p className="text-gray-500 text-sm">{farmer.district}, {farmer.state}</p>
        </div>
        <button
          onClick={() => { logout(); toast.success('Logged out'); }}
          className="text-sm text-red-600 font-medium"
        >
          Logout
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Link
          to="/weather"
          className="bg-white rounded-xl shadow-sm p-6 border border-gray-100 text-center text-green-700 font-medium hover:shadow-md transition-shadow flex items-center justify-center"
        >
          View weather & forecast →
        </Link>
        <Link
          to="/crops"
          className="bg-white rounded-xl shadow-sm p-6 border border-gray-100 text-center text-green-700 font-medium hover:shadow-md transition-shadow flex items-center justify-center"
        >
          View & manage your crops →
        </Link>
        <Link
          to="/future"
          className="bg-white rounded-xl shadow-sm p-6 border border-gray-100 text-center text-gray-600 font-medium hover:shadow-md transition-shadow flex items-center justify-center"
        >
          Upcoming features →
        </Link>
      </div>
    </motion.div>
  );
}