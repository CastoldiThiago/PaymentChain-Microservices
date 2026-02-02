import React from 'react';
import { Link, useLocation } from 'react-router-dom';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;

  return (
    <>
      <nav className="nav-bar">
        <div className="nav-content">
          <Link
            to="/customers"
            className={`nav-link ${isActive('/customers') ? 'active' : ''}`}
          >
            👥 Customers
          </Link>
          <Link
            to="/accounts"
            className={`nav-link ${isActive('/accounts') ? 'active' : ''}`}
          >
            💳 Accounts
          </Link>
          <Link
            to="/account-products"
            className={`nav-link ${isActive('/account-products') ? 'active' : ''}`}
          >
            📦 Products
          </Link>
          <Link
            to="/transactions"
            className={`nav-link ${isActive('/transactions') ? 'active' : ''}`}
          >
            💸 Transactions
          </Link>
        </div>
      </nav>
      <div className="app-container">{children}</div>
    </>
  );
};
