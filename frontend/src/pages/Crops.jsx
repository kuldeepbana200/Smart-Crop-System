import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'motion/react';
import toast from 'react-hot-toast';
import { getCrops } from '../services/cropService';
import CropCard from '../components/crop/CropCard';

export default function Crops() {
  const [crops, setCrops] = useState([]);
  const [status, setStatus] = useState('loading');

  useEffect(() => {
    getCrops()
      .then((data) => {
        setCrops(data);
        setStatus('ready');
      })
      .catch(() => {
        setStatus('error');
        toast.error('Could not load your crops.');
      });
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-green-700">My Crops</h1>
        <Link
          to="/crops/new"
          className="bg-green-600 text-white text-sm font-semibold px-4 py-2 rounded-lg hover:bg-green-700"
        >
          + Add Crop
        </Link>
      </div>

      {status === 'loading' && <p className="text-gray-500">Loading your crops...</p>}

      {status === 'error' && (
        <p className="text-red-600">Something went wrong loading your crops. Please try again later.</p>
      )}

      {status === 'ready' && crops.length === 0 && (
        <div className="bg-white rounded-xl shadow-sm p-10 text-center border border-dashed border-gray-200">
          <p className="text-gray-500 mb-4">You haven't added any crops yet.</p>
          <Link to="/crops/new" className="text-green-700 font-medium hover:underline">
            Add your first crop
          </Link>
        </div>
      )}

      {status === 'ready' && crops.length > 0 && (
        <motion.div
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5"
          initial="hidden"
          animate="visible"
          variants={{ visible: { transition: { staggerChildren: 0.08 } } }}
        >
          {crops.map((crop) => (
            <CropCard key={crop.id} crop={crop} />
          ))}
        </motion.div>
      )}
    </div>
  );
}