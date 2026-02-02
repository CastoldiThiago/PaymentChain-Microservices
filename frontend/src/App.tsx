import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthProvider';
import { NotificationProvider } from './context/NotificationContext';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { Layout } from './components/Layout';
import { Customers } from './pages/Customers';
import { Accounts } from './pages/Accounts';
import { AccountProducts } from './pages/AccountProducts';
import { Transactions } from './pages/Transactions';

const App: React.FC = () => (
  <AuthProvider>
    <NotificationProvider>
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<Navigate to="/customers" replace />} />
            <Route path="/login" element={<div>Login handled by Keycloak redirect</div>} />
            <Route path="/customers" element={<ProtectedRoute><Customers /></ProtectedRoute>} />
            <Route path="/accounts" element={<ProtectedRoute><Accounts /></ProtectedRoute>} />
            <Route path="/account-products" element={<ProtectedRoute><AccountProducts /></ProtectedRoute>} />
            <Route path="/transactions" element={<ProtectedRoute><Transactions /></ProtectedRoute>} />
            <Route path="*" element={<div>404 Not Found</div>} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </NotificationProvider>
  </AuthProvider>
);

export default App;
