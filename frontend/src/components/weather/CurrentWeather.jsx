import { motion } from 'motion/react';

export default function CurrentWeather({ data }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-white rounded-xl shadow-sm p-6 border border-gray-100"
    >
      <h2 className="text-lg font-semibold text-gray-800 mb-4">Current Weather</h2>
      <div className="grid grid-cols-2 gap-4 text-sm">
        <div>
          <p className="text-gray-400">Temperature</p>
          <p className="text-xl font-bold text-gray-800">{data.temperature}°C</p>
        </div>
        <div>
          <p className="text-gray-400">Humidity</p>
          <p className="text-xl font-bold text-gray-800">{data.relativeHumidity}%</p>
        </div>
        <div>
          <p className="text-gray-400">Precipitation</p>
          <p className="text-xl font-bold text-gray-800">{data.precipitation} mm</p>
        </div>
        <div>
          <p className="text-gray-400">Wind Speed</p>
          <p className="text-xl font-bold text-gray-800">{data.windSpeed} km/h</p>
        </div>
      </div>
    </motion.div>
  );
}