import React, { createContext, useContext, useState, useEffect } from "react";
import axios from "axios";
import i18n from "../i18n";

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem("token") || null);
  const [loading, setLoading] = useState(true);
  const [preferredLanguage, setPreferredLanguage] = useState("en");
  const [hasProfile, setHasProfile] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);

  const applyLanguage = (languageCode) => {
    const code = languageCode || "en";
    setPreferredLanguage(code);
    localStorage.setItem("language", code);
    i18n.changeLanguage(code);
  };

  // Initialize auth state on load
  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem("token");
      const storedUser = localStorage.getItem("user");

      if (storedToken && storedUser) {
        try {
          setToken(storedToken);
          const userData = JSON.parse(storedUser);
          setUser(userData);
          const userLang = userData.preferredLanguage || localStorage.getItem("language") || "en";
          applyLanguage(userLang);
        } catch (error) {
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          applyLanguage("en");
        }
      } else {
        const storedLang = localStorage.getItem("language") || "en";
        applyLanguage(storedLang);
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  // Check if user has a farmer profile
  const checkFarmerProfile = async () => {
    if (!token) {
      setProfileLoading(false);
      return;
    }

    setProfileLoading(true);
    try {
      const response = await axios.get("/api/farmers/profile", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      // If we get a successful response, user has a profile
      setHasProfile(true);
    } catch (error) {
      if (error.response && error.response.status === 401) {
        // Token is expired or invalid
        logout();
        return;
      }
      // If we get a 404 or other error, user doesn't have a profile yet
      // 404 is expected for new users who haven't created profile yet
      if (error.response && error.response.status === 404) {
        setHasProfile(false);
      } else {
        // For other errors, we'll assume no profile to be safe
        setHasProfile(false);
        console.error("Error checking farmer profile:", error);
      }
    } finally {
      setProfileLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      const response = await axios.post("/api/auth/login", {
        email,
        password,
      });

      const {
        token,
        userId,
        name,
        email: userEmail,
        role,
        preferredLanguage,
        phoneVerified,
      } = response.data;

      const userData = {
        userId,
        name,
        email: userEmail,
        role,
        preferredLanguage,
        phoneVerified,
      };

      setToken(token);
      setUser(userData);
      setHasProfile(false);
      setProfileLoading(false);
      applyLanguage(preferredLanguage || "en");

      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(userData));

      return { success: true, user: userData };
    } catch (error) {
      throw error.response?.data || { message: "Login failed" };
    }
  };

  const register = (name, email, password, phone, language = "en") => {
    return axios
      .post("/api/auth/register", {
        name,
        email,
        password,
        phone,
        language,
      })
      .then(async (response) => {
        const {
          token,
          userId,
          name: userName,
          email: userEmail,
          role,
          preferredLanguage,
          phoneVerified,
        } = response.data;

        const userData = {
          userId,
          name: userName,
          email: userEmail,
          role,
          preferredLanguage,
          phoneVerified,
        };

        setToken(token);
        setUser(userData);
        setHasProfile(false);
        setProfileLoading(false);
        applyLanguage(preferredLanguage || language || "en");

        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(userData));

        return { success: true, user: userData };
      });
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setPreferredLanguage("en");
    setHasProfile(false);
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    localStorage.setItem("language", "en");
    i18n.changeLanguage("en");

    delete axios.defaults.headers.common["Authorization"];
  };

  const markPhoneVerified = () => {
    setUser((currentUser) => {
      if (!currentUser) return currentUser;
      const updatedUser = { ...currentUser, phoneVerified: true };
      localStorage.setItem("user", JSON.stringify(updatedUser));
      return updatedUser;
    });
  };

  // Set axios default auth header when token changes
  useEffect(() => {
    if (token) {
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    } else {
      delete axios.defaults.headers.common["Authorization"];
    }
  }, [token]);

  // Keep the profile existence check available for non-gating use cases, but do not
  // use it as proof that a user's farm setup is complete.
  useEffect(() => {
    if (token) {
      checkFarmerProfile();
    } else {
      setHasProfile(false);
      setProfileLoading(false);
    }
  }, [token]);

  const value = {
    user,
    token,
    isAuthenticated: !!token,
    loading,
    preferredLanguage,
    hasProfile,
    profileLoading,
    login,
    register,
    logout,
    markPhoneVerified,
    refetchProfile: checkFarmerProfile,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
