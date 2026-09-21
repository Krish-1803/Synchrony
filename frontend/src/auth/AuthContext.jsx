import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import api, { setToken, getToken } from '../api/client.js';
import { DEMO_ACCOUNTS } from './demo.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadProfile = useCallback(async () => {
    if (!getToken()) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      const { data } = await api.get('/api/auth/me');
      setUser(data);
    } catch {
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const login = async (username, password) => {
    const { data } = await api.post('/api/auth/login', { username, password });
    setToken(data.token);
    await loadProfile();
    return data;
  };

  /**
   * Auto-authenticate as the demo account for a role. Used to open the portals
   * directly without a login screen. If the current session is already that
   * role, it is reused.
   */
  const demoLogin = async (role) => {
    if (user && user.role === role && getToken()) {
      return user;
    }
    const creds = DEMO_ACCOUNTS[role];
    if (!creds) {
      throw new Error(`No demo account for role ${role}`);
    }
    return login(creds.username, creds.password);
  };

  const register = async (payload) => {
    const { data } = await api.post('/api/auth/register', payload);
    setToken(data.token);
    await loadProfile();
    return data;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, demoLogin, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
