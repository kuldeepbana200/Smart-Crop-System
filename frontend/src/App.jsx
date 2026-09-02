import { BrowserRouter, Routes, Route, Navigate, Link } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import LanguageSelector from "./components/common/LanguageSelector";
import Login from "./pages/Login";
import Register from "./pages/Register";
import VerifyOTP from "./pages/VerifyOTP";
import Onboarding from "./pages/Onboarding";
import Welcome from "./pages/Welcome";
import Dashboard from "./pages/FarmerDashboard";
import MyCrops from "./pages/MyCrops";
import Weather from "./pages/Weather";
import Advisories from "./pages/Advisories";
import RiskAlerts from "./pages/RiskAlerts";
import MarketPrices from "./pages/MarketPrices";
import Education from "./pages/Education";
import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";
import ProtectedRoute from "./components/common/ProtectedRoute";
import "./App.css";
import { useState } from "react";

// Custom route for root path that redirects based on profile status
const RootRoute = () => {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex h-[100vh] items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!user?.phoneVerified) {
    return <Navigate to="/verify-otp" replace />;
  }

  return <Navigate to="/dashboard" replace />;
};

function App() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const closeMobileMenu = () => {
    setMobileMenuOpen(false);
  };

  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-gray-50">
          <header className="bg-white shadow-md">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex justify-between h-16">
                {/* Logo + Desktop Navigation */}
                <div className="flex items-center">
                  {/* Logo */}
                  <div className="flex-shrink-0">
                    <Link to="/dashboard">
                      <img
                        className="h-8 w-auto"
                        src="https://via.placeholder.com/40"
                        alt="Smart Crop Logo"
                      />
                    </Link>
                  </div>

                  {/* Desktop Navigation */}
                  <div className="hidden md:block">
                    <div className="ml-10 flex items-baseline space-x-4">
                      <Link
                        to="/dashboard"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Dashboard
                      </Link>

                      <Link
                        to="/crops"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        My Crops
                      </Link>

                      <Link
                        to="/weather"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Weather
                      </Link>

                      <Link
                        to="/advisories"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Advisories
                      </Link>

                      <Link
                        to="/alerts"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Alerts
                      </Link>

                      <Link
                        to="/market"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Market Prices
                      </Link>

                      <Link
                        to="/education"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Education
                      </Link>

                      <Link
                        to="/notifications"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Notifications
                      </Link>

                      <Link
                        to="/profile"
                        className="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900"
                      >
                        Profile
                      </Link>
                    </div>
                  </div>
                </div>

                {/* Language Selector */}
                <div className="hidden md:block">
                  <div className="ml-4 flex items-center md:ml-6">
                    <LanguageSelector />
                  </div>
                </div>

                {/* Mobile Menu Button */}
                <div className="-mr-2 flex md:hidden items-center">
                  <button
                    type="button"
                    onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                    className="bg-white rounded-md p-2 inline-flex items-center justify-center text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none"
                    aria-label="Toggle navigation menu"
                    aria-expanded={mobileMenuOpen}
                  >
                    <svg
                      className="h-6 w-6"
                      stroke="currentColor"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      {mobileMenuOpen ? (
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="2"
                          d="M6 18L18 6M6 6l12 12"
                        />
                      ) : (
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="2"
                          d="M4 6h16M4 12h16M4 18h16"
                        />
                      )}
                    </svg>
                  </button>
                </div>
              </div>

              {/* Mobile Menu */}
              {mobileMenuOpen && (
                <div className="md:hidden border-t border-gray-200">
                  <div className="px-2 pt-2 pb-3 space-y-1">
                    <Link
                      to="/dashboard"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Dashboard
                    </Link>

                    <Link
                      to="/crops"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      My Crops
                    </Link>

                    <Link
                      to="/weather"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Weather
                    </Link>

                    <Link
                      to="/advisories"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Advisories
                    </Link>

                    <Link
                      to="/alerts"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Alerts
                    </Link>

                    <Link
                      to="/market"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Market Prices
                    </Link>

                    <Link
                      to="/education"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Education
                    </Link>

                    <Link
                      to="/notifications"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Notifications
                    </Link>

                    <Link
                      to="/profile"
                      onClick={closeMobileMenu}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900"
                    >
                      Profile
                    </Link>
                  </div>
                </div>
              )}
            </div>
          </header>

          <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
            <Routes>
              {/* Public routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/verify-otp" element={<VerifyOTP />} />

              {/* Protected routes - onboarding and welcome skip profile check */}
              <Route
                path="/onboarding"
                element={
                  // <ProtectedRoute skipProfileCheck>
                  <Onboarding />
                  // </ProtectedRoute>
                }
              />
              <Route
                path="/welcome"
                element={
                  //<ProtectedRoute skipProfileCheck>
                  <Welcome />
                  //</ProtectedRoute>
                }
              />

              {/* Protected routes - require profile */}
              <Route path="/" element={<RootRoute />} />
              <Route
                path="/dashboard"
                element={
                  //<ProtectedRoute>
                  <Dashboard />
                  // </ProtectedRoute>
                }
              />
              <Route
                path="/crops"
                element={
                  // <ProtectedRoute>
                  <MyCrops />
                  //</ProtectedRoute>
                }
              />
              <Route
                path="/weather"
                element={
                  //<ProtectedRoute>
                  <Weather />
                  // </ProtectedRoute>
                }
              />
              <Route
                path="/advisories"
                element={
                  /// <ProtectedRoute>
                  <Advisories />
                  /// </ProtectedRoute>
                }
              />
              <Route
                path="/alerts"
                element={
                  // <ProtectedRoute>
                  <RiskAlerts />
                  // </ProtectedRoute>
                }
              />
              <Route
                path="/market"
                element={
                  //<ProtectedRoute>
                  <MarketPrices />
                  /// </ProtectedRoute>
                }
              />
              <Route
                path="/education"
                element={
                  //  <ProtectedRoute>
                  <Education />
                  ///</ProtectedRoute>
                }
              />
              <Route
                path="/notifications"
                element={
                  // <ProtectedRoute>
                  <Notifications />
                  // </ProtectedRoute>
                }
              />
              <Route
                path="/profile"
                element={
                  // <ProtectedRoute>
                  <Profile />
                  //</ProtectedRoute>
                }
              />

              {/* Catch-all */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
