import { motion } from 'motion/react';

export default function ForecastList({ daily }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-white rounded-xl shadow-sm p-6 border border-gray-100"
    >
      <h2 className="text-lg font-semibold text-gray-800 mb-4">Forecast</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {daily.map((day, idx) => (
          <div key={idx} className="border border-gray-100 rounded-lg p-3 text-sm">
            <p className="font-medium text-gray-700 mb-1">{day.date || `Day ${idx + 1}`}</p>
            <p className="text-gray-500">Max: {day.maxTemperature}°C · Min: {day.minTemperature}°C</p>
            <p className="text-gray-500">Rain: {day.precipitationSum} mm ({day.precipitationProbability}%)</p>
            <p className="text-gray-500">Max wind: {day.maxWindSpeed} km/h</p>
          </div>
        ))}
      </div>
    </motion.div>
  );
}