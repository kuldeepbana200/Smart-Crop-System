import axios from "axios";

// Always use the Vite proxy in local development.
// Browser: localhost:5173/api/...
// Vite proxy: localhost:8080/api/...
const api = axios.create({
  baseURL: "/api",
  timeout: 20000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Add JWT to every authenticated request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    console.log(
      `[API] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`
    );

    return config;
  },
  (error) => Promise.reject(error)
);

// Common response/error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error(
      "[API ERROR]",
      error.response?.status,
      error.config?.url,
      error.response?.data || error.message
    );

    return Promise.reject(error);
  }
);

export default api;


// =========================
// AUTH
// =========================

export const authService = {
  login: (email, password) =>
    api.post("/auth/login", {
      email,
      password,
    }),

  register: (userData) =>
    api.post("/auth/register", userData),

  sendOTP: (email) =>
    api.post("/auth/send-otp", {
      email,
    }),

  verifyOTP: (otp) =>
    api.post("/auth/verify-otp", {
      otp,
    }),

  resendOTP: (email) =>
    api.post("/auth/resend-otp", {
      email,
    }),

  getLanguage: () =>
    api.get("/auth/language"),

  setLanguage: (language) =>
    api.put("/auth/language", language, {
      headers: {
        "Content-Type": "text/plain",
      },
    }),
};


// =========================
// FARMER
// =========================

export const farmerService = {
  getProfile: () =>
    api.get("/farmers/me"),

  createProfile: (profileData) =>
    api.post("/farmers/profile", profileData),

  updateProfile: (profileData) =>
    api.put("/farmers/me", profileData),
};


// =========================
// CROPS
// =========================

export const cropService = {
  getCrops: () =>
    api.get("/crops"),

  createCrop: (cropData) =>
    api.post("/crops", cropData),

  getCrop: (id) =>
    api.get(`/crops/${id}`),

  updateCrop: (id, cropData) =>
    api.put(`/crops/${id}`, cropData),

  deleteCrop: (id) =>
    api.delete(`/crops/${id}`),

  logHarvest: (id, harvestData) =>
    api.post(`/crops/${id}/harvest`, harvestData),
};


// =========================
// WEATHER
// =========================

export const weatherService = {
  getCurrentWeather: () =>
    api.get("/weather/current"),

  getForecast: () =>
    api.get("/weather/forecast"),
};


// =========================
// ADVISORIES
// =========================

export const advisoryService = {
  getAdvisories: () =>
    api.get("/advisories"),

  getAdvisory: (id) =>
    api.get(`/advisories/${id}`),

  generateAdvisory: (cropId) =>
    api.post("/advisories/generate", {
      cropId,
    }),

  dismissAdvisory: (id) =>
    api.post(`/advisories/${id}/dismiss`),

  shareAdvisory: (id) =>
    api.post(`/advisories/${id}/share`),
};


// =========================
// RISK
// =========================

export const riskService = {
  assessRisk: (cropId) =>
    api.post("/risk/assess", {
      cropId,
    }),

  getRiskHistory: (cropId) =>
    api.get(`/risk/history/${cropId}`),
};


// =========================
// ALERTS
// =========================

export const alertService = {
  getFarmerAlerts: () =>
    api.get("/farmers/me/alerts"),

  getAlert: (id) =>
    api.get(`/farmers/me/alerts/${id}`),

  createAlert: () =>
    api.post("/farmers/alerts"),
};


// =========================
// NOTIFICATIONS
// =========================

export const notificationService = {
  getNotifications: () =>
    api.get("/notifications"),

  getUnreadCount: () =>
    api.get("/notifications/unread"),

  markAsRead: (id) =>
    api.patch(`/notifications/${id}/read`),
};


// =========================
// MARKET
// =========================

export const marketService = {
  getPrices: (filters = {}) =>
    api.get("/market/prices", {
      params: filters,
    }),

  getPriceHistory: (filters = {}) =>
    api.get("/market/prices/history", {
      params: filters,
    }),

  comparePrices: (filters = {}) =>
    api.get("/market/prices/compare", {
      params: filters,
    }),
};

export const marketAdviceService = {
  getMarketAdvice: () => api.post("/ai/market-advice", {}),
};


// =========================
// EDUCATION
// =========================

export const educationService = {
  getAIEducation: () =>
    api.post("/ai/education", {}),
};