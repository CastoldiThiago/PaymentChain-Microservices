import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthProvider';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { Customers } from './pages/Customers';
// Importa Accounts, Transactions, Login...

const App: React.FC = () => (
  <AuthProvider>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/customers" replace />} />
        <Route path="/login" element={<div>Login handled by Keycloak redirect</div>} />
        <Route path="/customers" element={<ProtectedRoute><Customers /></ProtectedRoute>} />
        {/* Agrega Accounts, Transactions, etc */}
        <Route path="*" element={<div>404 Not Found</div>} />
      </Routes>
    </BrowserRouter>
  </AuthProvider>
);

export default App;
