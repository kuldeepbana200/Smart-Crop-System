import { Link } from 'react-router-dom';
import { motion } from 'motion/react';
import ComingSoonCard from '../components/common/ComingSoonCard';

export default function FutureFeatures() {
  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <Link to="/dashboard" className="text-sm text-green-700 hover:underline">← Back to dashboard</Link>
      <h1 className="text-2xl font-bold text-green-700 mt-4 mb-6">Upcoming Features</h1>

      <motion.div
        className="grid grid-cols-1 sm:grid-cols-2 gap-5"
        initial="hidden"
        animate="visible"
        variants={{ visible: { transition: { staggerChildren: 0.08 } } }}
      >
        <ComingSoonCard
          icon="🌾"
          title="Crop Advisory"
          description="Plain-language, regional-language advisory based on your crop stage, weather, and soil data."
        />
        <ComingSoonCard
          icon="📈"
          title="Market Prices"
          description="Compare mandi prices near you before deciding when and where to sell."
        />
        <ComingSoonCard
          icon="⚠️"
          title="Distress-Risk Score"
          description="A simple risk score combining rainfall, price trends, and loan due dates to flag when you may need support."
        />
        <ComingSoonCard
          icon="👨‍🌾"
          title="Officer Alerts"
          description="Local agriculture officers will be notified automatically if your distress-risk score needs attention."
        />
      </motion.div>
    </div>
  );
}