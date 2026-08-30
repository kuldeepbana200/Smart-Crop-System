import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login(email, password);
      // Let the root route decide whether the user should go to onboarding or dashboard
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
            Sign in to your account
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Or
            <a href="#" className="font-medium text-indigo-600 hover:text-indigo-500">
              Start your trial
            </a>
          </p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <input
            type="email"
            name="email"
            id="email"
            autoComplete="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
            placeholder="Email address"
          />

          <div className="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              id="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Password"
            />
            <button
              type="button"
              className="absolute inset-y-0 right-0 flex items-center pr-2"
              onClick={() => setShowPassword(!showPassword)}
              aria-label="Show password"
            >
              {showPassword ? (
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112.19 9c1.12-1.093 2.267-1.995 3.55-2.65 1.03-.492 2.042-.94 2.976-1.295.094-.029.188-.055.281-.078a1.002 1.002 0 00-1.96-.075c-.463.065-.874.226-1.08.507A11.947 11.947 0 0012 18.75a11.95 11.95 0 006.22-2.074c-.206.281-.348.614-.418.977-.076.386-.12.802-.12 1.235z"></path>
                </svg>
              ) : (
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112.19 9c1.12-1.093 2.267-1.995 3.55-2.65 1.03-.492 2.042-.94 2.976-1.295.094-.029.188-.055.281-.078a1.002 1.002 0 001.96-.075c.463.065.874.226 1.08.507A11.947 11.947 0 0112 18.75a11.95 11.95 0 01-6.22-2.074c.206.281.348.614.418.977.076.386.12.802.12 1.235z"></path>
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4.112 7.869a1.01 1.01 0 00-1.378.58l-.777 1.777A1.01 1.01 0 002.142 12.015a1.01 1.01 0 00.558 1.378l1.777.777a1.01 1.01 0 001.378-.58l.777-1.777A1.01 1.01 0 004.112 7.869zM14.25 4.875c0-1.11.89-2 2-2s2 .89 2 2-.89 2-2 2-2-.89-2-2z"></path>
                </svg>
              )}
            </button>
          </div>

          {error && (
            <div className="bg-red-50 text-red-500 text-sm mt-2 p-2 rounded">
              {error}
            </div>
          )}

          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input
                type="checkbox"
                id="remember-me"
                className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
              />
              <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-900">
                Remember me
              </label>
            </div>

            <div className="text-sm">
              <a href="#" className="font-medium text-indigo-600 hover:text-indigo-500">
                Forgot password?
              </a>
            </div>
          </div>

          <button
            type="submit"
            className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
            disabled={loading}
          >
            {loading ? (
              <>
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="5"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"></path>
                </svg>
                Signing in...
              </>
            ) : (
              'Sign in'
            )}
          </button>

          <p className="mt-6 text-center text-sm">
            Don’t have an account?{' '}
            <a href="/register" className="font-medium text-indigo-600 hover:text-indigo-500">
              Sign up
            </a>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Login;