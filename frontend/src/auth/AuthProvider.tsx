import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import keycloak from './keycloak';

interface AuthContextType {
  keycloak: typeof keycloak;
  authenticated: boolean;
  initialized: boolean;
  login: () => void;
  logout: () => void;
  token?: string;
  user?: string;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [initialized, setInitialized] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [token, setToken] = useState<string | undefined>(undefined);
  const [user, setUser] = useState<string | undefined>(undefined);
  const initRef = useRef(false);

  useEffect(() => {
    if (initRef.current) return; // Prevent double initialization
    initRef.current = true;

    keycloak.init({ onLoad: 'login-required' }).then(auth => {
      setAuthenticated(auth);
      setInitialized(true);
      setToken(keycloak.token);
      setUser(keycloak.tokenParsed?.preferred_username);
    }).catch(error => {
      console.error('Keycloak init error:', error);
      setInitialized(true);
    });

    keycloak.onAuthSuccess = () => {
      setAuthenticated(true);
      setToken(keycloak.token);
      setUser(keycloak.tokenParsed?.preferred_username);
    };
    keycloak.onAuthLogout = () => {
      setAuthenticated(false);
      setToken(undefined);
      setUser(undefined);
    };
  }, []);

  const login = () => keycloak.login();
  const logout = () => keycloak.logout();

  return (
    <AuthContext.Provider value={{ keycloak, authenticated, initialized, login, logout, token, user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
