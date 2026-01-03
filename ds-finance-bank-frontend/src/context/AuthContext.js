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

    const { email, role, customerId, customerNumber } = JSON.parse(auth);

    authService
      .validate()
      .then((validatedUser) => {
        setUser(validatedUser || { email, role, customerId, customerNumber });
      })
      .catch(() => {
        localStorage.removeItem('auth');
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (email, password) => {
    const authData = await authService.login(email, password);
    localStorage.setItem('auth', JSON.stringify(authData));
    setUser({
      email: authData.email || email,
      role: authData.role,
      customerId: authData.customerId,
      customerNumber: authData.customerNumber,
    });
    return authData;
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
