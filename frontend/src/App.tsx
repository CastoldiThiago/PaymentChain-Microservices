import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom';
import { AuthProvider } from './auth/AuthProvider';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { Customers } from './pages/Customers';
import { Accounts } from './pages/Accounts';
import { AccountProducts } from './pages/AccountProducts';
import { Transactions } from './pages/Transactions';

const App: React.FC = () => (
  <AuthProvider>
    <BrowserRouter>
      <nav style={{ display: 'flex', gap: '12px', padding: '12px 0' }}>
        <Link to="/customers">Customers</Link>
        <Link to="/accounts">Accounts</Link>
        <Link to="/account-products">Account Products</Link>
        <Link to="/transactions">Transactions</Link>
      </nav>
      <Routes>
        <Route path="/" element={<Navigate to="/customers" replace />} />
        <Route path="/login" element={<div>Login handled by Keycloak redirect</div>} />
        <Route path="/customers" element={<ProtectedRoute><Customers /></ProtectedRoute>} />
        <Route path="/accounts" element={<ProtectedRoute><Accounts /></ProtectedRoute>} />
        <Route path="/account-products" element={<ProtectedRoute><AccountProducts /></ProtectedRoute>} />
        <Route path="/transactions" element={<ProtectedRoute><Transactions /></ProtectedRoute>} />
        <Route path="*" element={<div>404 Not Found</div>} />
      </Routes>
    </BrowserRouter>
  </AuthProvider>
);

export default App;
