import { Link } from 'react-router-dom';
import { motion } from 'motion/react';

export default function CropCard({ crop }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-white rounded-xl shadow-sm p-5 border border-gray-100 hover:shadow-md transition-shadow"
    >
      <div className="flex justify-between items-start">
        <div>
          <h3 className="font-semibold text-gray-800">{crop.cropName}</h3>
          <span className="inline-block mt-1 text-xs font-medium bg-green-100 text-green-700 px-2 py-0.5 rounded-full">
            {crop.cropStage}
          </span>
        </div>
      </div>
      <div className="text-sm text-gray-500 mt-3 space-y-1">
        <p>Sown: {crop.sowingDate}</p>
        <p>Expected harvest: {crop.expectedHarvestDate}</p>
      </div>
      <Link
        to={`/crops/${crop.id}`}
        className="inline-block mt-4 text-sm font-medium text-green-700 hover:underline"
      >
        View details →
      </Link>
    </motion.div>
  );
}