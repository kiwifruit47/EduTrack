import { createContext, useState, useEffect, useCallback } from "react";
import api from "../api/axiosInstance";
import jwtDecode from "jwt-decode";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);
  const [user, setUser] = useState(null);
  const REFRESH_INTERVAL = 15 * 60 * 1000; // 15 minutes

// Login
  const login = async (email, password) => {
    try {
      const res = await api.post("/auth/login", { email, password });
      const token = res.data.accessToken;

      setAccessToken(token);

      const decoded = jwtDecode(token);
      setUser(decoded);

      return { success: true };
    } catch (err) {
      console.error("Login error:", err);
      return { success: false, error: err.response?.data?.message };
    }
  };

// Logout
  const logout = async () => {
    try {
      await api.post("/auth/logout");
    } catch (err) {
      console.error(err);
    }
    setAccessToken(null);
    setUser(null);
  };

// Refresh token
  const refreshToken = useCallback(async () => {
    try {
      const res = await api.post("/auth/refresh");
      const token = res.data.accessToken;

      setAccessToken(token);
      const decoded = jwtDecode(token);
      setUser(decoded);

    } catch (err) {
      console.error("Refresh token failed:", err);
      logout();
    }
  }, []);

  // Refresh token every 15 min
  useEffect(() => {
    const interval = setInterval(() => {
      refreshToken();
    }, REFRESH_INTERVAL);

    return () => clearInterval(interval);
  }, [refreshToken]);

// Attaching access token to all requests
  useEffect(() => {
    const interceptor = api.interceptors.request.use(
      (config) => {
        if (accessToken) {
          config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    return () => {
      api.interceptors.request.eject(interceptor);
    };
  }, [accessToken]);

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        login,
        logout,
        isAuthenticated: !!accessToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
