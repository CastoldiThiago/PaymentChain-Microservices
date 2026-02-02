import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';

export const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { authenticated, initialized } = useAuth();
  if (!initialized) return <div>Loading...</div>;
  return authenticated ? <>{children}</> : <Navigate to="/login" />;
};
