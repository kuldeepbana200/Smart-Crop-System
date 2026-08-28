import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'motion/react';
import toast from 'react-hot-toast';
import CropForm from '../components/crop/CropForm';
import { createCrop } from '../services/cropService';

export default function AddCrop() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (form) => {
    setLoading(true);
    try {
      await createCrop(form);
      toast.success('Crop added');
      navigate('/crops');
    } catch (err) {
      toast.error('Could not add crop. Please check your details.');
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
      <div>
        <h1 className="text-2xl font-bold text-green-700 mb-6 text-center">Add Crop</h1>
        <CropForm onSubmit={handleSubmit} loading={loading} submitLabel="Add Crop" />
      </div>
    </motion.div>
  );
}