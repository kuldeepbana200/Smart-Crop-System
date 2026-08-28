import { motion } from 'motion/react';

export default function OfficerDashboard() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="min-h-screen bg-blue-50 p-8"
    >
      <h1 className="text-2xl font-bold text-blue-700 mb-2">Agriculture Officer Dashboard</h1>
      <p className="text-gray-600 mb-8">
        This module is coming soon. Officers will be able to view distress-risk alerts and flagged farmers here once the backend module is ready.
      </p>
      <div className="bg-white rounded-xl shadow-sm p-6 border border-dashed border-blue-200 text-center text-gray-400">
        Distress-risk alerts and farmer flagging will appear here
      </div>
    </motion.div>
  );
}