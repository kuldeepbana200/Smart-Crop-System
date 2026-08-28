import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { motion } from 'motion/react';
import toast from 'react-hot-toast';
import CropForm from '../components/crop/CropForm';
import { getCropById, updateCrop } from '../services/cropService';

export default function EditCrop() {
  const { id } = useParams();
  const [initialValues, setInitialValues] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    getCropById(id)
      .then((data) =>
        setInitialValues({
          cropName: data.cropName,
          cropStage: data.cropStage,
          sowingDate: data.sowingDate,
          expectedHarvestDate: data.expectedHarvestDate,
        })
      )
      .catch(() => toast.error('Could not load crop details.'));
  }, [id]);

  const handleSubmit = async (form) => {
    setLoading(true);
    try {
      await updateCrop(id, form);
      toast.success('Crop updated');
      navigate(`/crops/${id}`);
    } catch (err) {
      toast.error('Could not update crop.');
    } finally {
      setLoading(false);
    }
  };

  if (!initialValues) {
    return <div className="min-h-screen flex items-center justify-center text-gray-500">Loading...</div>;
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="min-h-screen flex items-center justify-center bg-green-50 px-4"
    >
      <div>
        <h1 className="text-2xl font-bold text-green-700 mb-6 text-center">Edit Crop</h1>
        <CropForm initialValues={initialValues} onSubmit={handleSubmit} loading={loading} submitLabel="Save Changes" />
      </div>
    </motion.div>
  );
}