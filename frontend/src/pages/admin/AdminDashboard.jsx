import { motion } from 'motion/react';

export default function AdminDashboard() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="min-h-screen bg-purple-50 p-8"
    >
      <h1 className="text-2xl font-bold text-purple-700 mb-2">Admin Dashboard</h1>
      <p className="text-gray-600 mb-8">
        This module is coming soon. Admins will manage farmers, officers, and system-wide settings here once the backend module is ready.
      </p>
      <div className="bg-white rounded-xl shadow-sm p-6 border border-dashed border-purple-200 text-center text-gray-400">
        User management and system settings will appear here
      </div>
    </motion.div>
  );
}