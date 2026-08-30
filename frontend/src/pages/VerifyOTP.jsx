import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";

const VerifyOTP = () => {
  const [loading, setLoading] = useState(false);
  const [verified, setVerified] = useState(false);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState("");
  const [resendLoading, setResendLoading] = useState(false);
  const [otp, setOTP] = useState("");
  const navigate = useNavigate();
  const location = useLocation();
  const { user, markPhoneVerified } = useAuth();

  const verifyOTP = async () => {
    if (!otp.trim()) {
      setError("Please enter the OTP code");
      return;
    }

    setLoading(true);
    try {
      await api.post("/auth/verify-otp", otp, { headers: { "Content-Type": "text/plain" } });
      markPhoneVerified();
      setVerified(true);
      setMessage("Your phone number has been successfully verified!");
    } catch (err) {
      setError(
        err.response?.data?.message || "Verification failed. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  const resendOTP = async () => {
    if (!user) {
      // If no user, redirect to login
      navigate("/login", { replace: true });
      return;
    }

    setResendLoading(true);
    try {
      await api.post("/auth/resend-otp", user.email, { headers: { "Content-Type": "text/plain" } });
      setMessage("OTP resent! Please check your phone.");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to resend OTP.");
    } finally {
      setResendLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center py-12">
        <div className="text-center">
          <div className="mb-6">
            <svg
              className="animate-spin h-8 w-8 text-indigo-600"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="5"
              ></circle>
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8v8z"
              ></path>
            </svg>
          </div>
          <p className="text-sm text-gray-500">
            Verifying your phone number...
          </p>
        </div>
      </div>
    );
  }

  if (verified) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-md space-y-6">
          <div className="text-center">
            <div className="mb-6">
              <svg
                className="h-12 w-12 text-green-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M5 13l4 4L19 7"
                ></path>
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900">
              Phone Verified!
            </h2>
            {message && <p className="mt-2 text-sm text-gray-600">{message}</p>}
            <p className="mt-4 text-sm text-gray-500">
              Your account is now verified. You can continue to complete your
              profile.
            </p>
            <div className="mt-6">
              <button
                onClick={() => navigate("/", { replace: true })}
                className="w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                Continue to Profile Setup
              </button>
            </div>
            <p className="mt-4 text-sm text-gray-500">
              Didn't receive the OTP?{" "}
              <button
                onClick={resendOTP}
                disabled={resendLoading}
                className="ml-2 text-sm font-medium text-indigo-600 hover:text-indigo-500 underline"
              >
                {resendLoading ? "Resending..." : "Resend OTP"}
              </button>
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center">
          <div className="mb-6">
            <svg
              className="h-12 w-12 text-indigo-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              ></path>
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-900">
            Verify your phone number
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            We've sent an OTP to your phone number. Please enter the 6-digit
            code to verify your account.
          </p>

          <div className="mt-4">
            <input
              type="text"
              value={otp}
              onChange={(e) => setOTP(e.target.value)}
              maxLength="6"
              className="appearance-none block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm text-center letter-spacing-wide"
              placeholder="______"
              autoComplete="off"
            />
          </div>

          {error && (
            <div className="bg-red-50 text-red-500 text-sm mt-4 p-2 rounded">
              {error}
            </div>
          )}

          <div className="mt-6">
            <button
              onClick={verifyOTP}
              className="w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
              disabled={loading || !otp.trim()}
            >
              {loading ? (
                <>
                  <svg
                    className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="5"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v8z"
                    ></path>
                  </svg>
                  Verifying...
                </>
              ) : (
                "Verify OTP"
              )}
            </button>
          </div>

          <div className="mt-4">
            <button
              onClick={() => navigate("/login", { replace: true })}
              className="w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-gray-200 hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
            >
              Back to Login
            </button>
          </div>

          <div className="mt-6 text-center text-sm">
            Didn't receive the OTP?{" "}
            <button
              onClick={resendOTP}
              disabled={resendLoading}
              className="ml-2 text-sm font-medium text-indigo-600 hover:text-indigo-500 underline"
            >
              {resendLoading ? "Resending..." : "Resend OTP"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerifyOTP;
