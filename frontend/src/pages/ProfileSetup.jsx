import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'motion/react';
import { createFarmerProfile } from '../services/farmerService';
import toast from 'react-hot-toast';

export default function ProfileSetup() {
  const [form, setForm] = useState({ district: '', state: '', latitude: '', longitude: '', landArea: '' });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await createFarmerProfile({
        district: form.district,
        state: form.state,
        latitude: parseFloat(form.latitude),
        longitude: parseFloat(form.longitude),
        landArea: parseFloat(form.landArea),
      });
      toast.success('Profile saved');
      navigate('/dashboard');
    } catch (err) {
      toast.error('Could not save profile. Please check your details.');
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
        <h1 className="text-2xl font-bold text-green-700 mb-2">Complete Your Profile</h1>
        <p className="text-sm text-gray-500 mb-6">We need this to show local weather and crop advisory.</p>
        <input name="district" placeholder="District" value={form.district} onChange={handleChange} className="w-full border rounded-lg px-3 py-2 mb-4" required />
        <input name="state" placeholder="State" value={form.state} onChange={handleChange} className="w-full border rounded-lg px-3 py-2 mb-4" required />
        <input name="latitude" type="number" step="any" placeholder="Latitude" value={form.latitude} onChange={handleChange} className="w-full border rounded-lg px-3 py-2 mb-4" required />
        <input name="longitude" type="number" step="any" placeholder="Longitude" value={form.longitude} onChange={handleChange} className="w-full border rounded-lg px-3 py-2 mb-4" required />
        <input name="landArea" type="number" step="any" placeholder="Land Area (acres)" value={form.landArea} onChange={handleChange} className="w-full border rounded-lg px-3 py-2 mb-4" required />
        <button type="submit" disabled={loading} className="w-full bg-green-600 text-white rounded-lg py-2 font-semibold hover:bg-green-700 disabled:opacity-50">
          {loading ? 'Saving...' : 'Save Profile'}
        </button>
      </form>
    </motion.div>
  );
}