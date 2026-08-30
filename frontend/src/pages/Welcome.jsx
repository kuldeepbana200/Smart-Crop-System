import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Welcome = () => {
  const navigate = useNavigate();
  const { refetchProfile } = useAuth();

  useEffect(() => {
    refetchProfile();
  }, [refetchProfile]);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center py-12">
      <div className="text-center">
        <div className="mb-8">
          <svg className="h-12 w-12 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
          </svg>
        </div>
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          Welcome to Smart Crop System
        </h1>
        <p className="text-xl text-gray-600 mb-8">
          Your profile has been successfully saved!
        </p>
        <button
          onClick={() => navigate('/', { replace: true })}
          className="px-6 py-3 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        >
          Go to Dashboard
        </button>
      </div>
    </div>
  );
};

export default Welcome;