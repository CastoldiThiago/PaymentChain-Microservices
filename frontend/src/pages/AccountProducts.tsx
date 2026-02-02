import React, { useEffect, useState } from 'react';
import {
  AccountProduct,
  createAccountProduct,
  getAccountProducts,
} from '../services/accountProduct.service';
import { useNotification } from '../context/NotificationContext';
import { getErrorMessage } from '../utils/errorHandler';

export const AccountProducts: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const [products, setProducts] = useState<AccountProduct[]>([]);
  const [form, setForm] = useState({ name: '', transactionFeePercentage: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadProducts = () => {
    getAccountProducts()
      .then(res => setProducts(res.data))
      .catch(error => {
        showError(`Failed to load products: ${getErrorMessage(error)}`);
      });
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;
    
    setIsSubmitting(true);
    createAccountProduct({
      name: form.name.trim(),
      transactionFeePercentage: Number(form.transactionFeePercentage),
    })
      .then(() => {
        showSuccess('Account Product created successfully!');
        setForm({ name: '', transactionFeePercentage: '' });
        loadProducts();
      })
      .catch(error => {
        showError(getErrorMessage(error));
      })
      .finally(() => {
        setIsSubmitting(false);
      });
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">Account Products</h1>
        <p className="page-subtitle">Define account types and their transaction fee structures</p>
      </div>

      <div className="card">
        <h3 className="card-title">Create New Product</h3>
        <form onSubmit={handleSubmit} className="form-grid">
          <div className="form-group">
            <label className="form-label">Product Name</label>
            <input name="name" value={form.name} onChange={handleChange} placeholder="e.g., Gold Checking Account" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Transaction Fee Percentage</label>
            <input
              name="transactionFeePercentage"
              value={form.transactionFeePercentage}
              onChange={handleChange}
              placeholder="0.01 = 1%"
              type="number"
              step="0.0001"
              className="form-input"
              required
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Creating...' : 'Create Product'}
            </button>
          </div>
        </form>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total Products</div>
          <div className="stat-value">{products.length}</div>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Product Name</th>
              <th>Transaction Fee</th>
            </tr>
          </thead>
          <tbody>
            {products.map(p => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td><strong>{p.name}</strong></td>
                <td>
                  <span className="badge badge-info">
                    {(p.transactionFeePercentage * 100).toFixed(2)}%
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
