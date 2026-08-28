import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { motion } from 'motion/react';
import toast from 'react-hot-toast';
import { getCropById, deleteCrop } from '../services/cropService';

export default function CropDetails() {
  const { id } = useParams();
  const [crop, setCrop] = useState(null);
  const [status, setStatus] = useState('loading');
  const navigate = useNavigate();

  useEffect(() => {
    getCropById(id)
      .then((data) => {
        setCrop(data);
        setStatus('ready');
      })
      .catch(() => setStatus('error'));
  }, [id]);

  const handleDelete = async () => {
    if (!window.confirm('Delete this crop? This cannot be undone.')) return;
    try {
      await deleteCrop(id);
      toast.success('Crop deleted');
      navigate('/crops');
    } catch (err) {
      toast.error('Could not delete crop.');
    }
  };

  if (status === 'loading') {
    return <div className="min-h-screen flex items-center justify-center text-gray-500">Loading...</div>;
  }

  if (status === 'error') {
    return (
      <div className="min-h-screen flex items-center justify-center text-red-600">
        Crop not found or you don't have access to it.
      </div>
    );
  }

  return (
    <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="min-h-screen bg-gray-50 p-8">
      <Link to="/crops" className="text-sm text-green-700 hover:underline">← Back to crops</Link>
      <div className="bg-white rounded-xl shadow-sm p-8 mt-4 max-w-md">
        <h1 className="text-2xl font-bold text-gray-800 mb-1">{crop.cropName}</h1>
        <span className="inline-block text-xs font-medium bg-green-100 text-green-700 px-2 py-0.5 rounded-full mb-4">
          {crop.cropStage}
        </span>
        <p className="text-sm text-gray-500">Sown: {crop.sowingDate}</p>
        <p className="text-sm text-gray-500 mb-6">Expected harvest: {crop.expectedHarvestDate}</p>
        <div className="flex gap-3">
          <Link
            to={`/crops/${id}/edit`}
            className="bg-gray-100 text-gray-700 text-sm font-medium px-4 py-2 rounded-lg hover:bg-gray-200"
          >
            Edit
          </Link>
          <button
            onClick={handleDelete}
            className="bg-red-50 text-red-600 text-sm font-medium px-4 py-2 rounded-lg hover:bg-red-100"
          >
            Delete
          </button>
        </div>
      </div>
    </motion.div>
  );
}