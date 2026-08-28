import { motion } from 'motion/react';

export default function ComingSoonCard({ title, description, icon }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-white rounded-xl shadow-sm p-6 border border-dashed border-gray-200"
    >
      <div className="flex items-center gap-3 mb-2">
        {icon && <span className="text-2xl">{icon}</span>}
        <h3 className="font-semibold text-gray-700">{title}</h3>
        <span className="ml-auto text-xs font-medium bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full">
          Coming soon
        </span>
      </div>
      <p className="text-sm text-gray-400">{description}</p>
    </motion.div>
  );
}