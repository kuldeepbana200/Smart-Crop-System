import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { getCurrentWeather, getWeatherForecast } from '../services/weatherService';
import CurrentWeather from '../components/weather/CurrentWeather';
import ForecastList from '../components/weather/ForecastList';

export default function Weather() {
  const [current, setCurrent] = useState(null);
  const [forecast, setForecast] = useState(null);
  const [status, setStatus] = useState('loading'); // loading | ready | needsProfile | unavailable | error

  useEffect(() => {
    Promise.all([getCurrentWeather(), getWeatherForecast()])
      .then(([currentData, forecastData]) => {
        setCurrent(currentData);
        setForecast(forecastData);
        setStatus('ready');
      })
      .catch((err) => {
        const code = err.response?.status;
        if (code === 422) {
          setStatus('needsProfile');
        } else if (code === 503) {
          setStatus('unavailable');
          toast.error('Weather service is temporarily unavailable.');
        } else {
          setStatus('error');
          toast.error('Could not load weather data.');
        }
      });
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <Link to="/dashboard" className="text-sm text-green-700 hover:underline">← Back to dashboard</Link>
      <h1 className="text-2xl font-bold text-green-700 mt-4 mb-6">Weather</h1>

      {status === 'loading' && <p className="text-gray-500">Loading weather data...</p>}

      {status === 'needsProfile' && (
        <div className="bg-white rounded-xl shadow-sm p-6 border border-dashed border-gray-200 text-center text-gray-500">
          Complete your farm profile (location) to see weather data.
        </div>
      )}

      {status === 'unavailable' && (
        <div className="bg-white rounded-xl shadow-sm p-6 border border-dashed border-gray-200 text-center text-gray-500">
          Weather service is temporarily unavailable. Please check back later.
        </div>
      )}

      {status === 'error' && (
        <p className="text-red-600">Something went wrong loading weather data.</p>
      )}

      {status === 'ready' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <CurrentWeather data={current} />
          <ForecastList daily={forecast.daily || forecast} />
        </div>
      )}
    </div>
  );
}