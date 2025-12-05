import React, { createContext, useState, useContext, useEffect } from 'react';
import { authService } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const auth = localStorage.getItem('auth');
    if (!auth) {
      setLoading(false);
      return;
    }

    const { username, role } = JSON.parse(auth);

    authService
      .validate()
      .then((validatedUser) => {
        setUser(validatedUser || { username, role });
      })
      .catch(() => {
        localStorage.removeItem('auth');
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (username, password, role) => {
    const userData = await authService.login(username, password);
    // Store username, role AND password for subsequent API calls
    const authData = { ...userData, password };
    localStorage.setItem('auth', JSON.stringify(authData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('auth');
    setUser(null);
  };

  const isEmployee = () => user?.role === 'employee';
  const isCustomer = () => user?.role === 'customer';

  return (
    <AuthContext.Provider value={{ user, login, logout, isEmployee, isCustomer, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
