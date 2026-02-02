import React, { useEffect, useState } from 'react';
import {
  Account,
  CreateAccountRequest,
  createAccount,
  deleteAccount,
  getAccounts,
  getAccountsByCustomer,
  updateAccount,
} from '../services/account.service';
import { CURRENCIES } from '../constants';

export const Accounts: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [customerFilter, setCustomerFilter] = useState('');
  const [form, setForm] = useState({
    iban: '',
    balance: '',
    customerId: '',
    productId: '',
    currency: 'USD',
  });

  const loadAccounts = (pageNumber: number = 0) => {
    const customerId = customerFilter.trim();
    if (customerId) {
      getAccountsByCustomer(customerId).then(res => {
        setAccounts(res.data);
        setPage(0);
        setTotalPages(1);
        setTotalElements(res.data.length);
      });
      return;
    }

    getAccounts({ page: pageNumber, size: 10 }).then(res => {
      setAccounts(res.data.content);
      setPage(res.data.number);
      setTotalPages(res.data.totalPages);
      setTotalElements(res.data.totalElements);
    });
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const buildPayload = (): CreateAccountRequest => ({
    iban: form.iban.trim(),
    balance: Number(form.balance),
    customerId: Number(form.customerId),
    productId: Number(form.productId),
    currency: form.currency.trim(),
  });

  const resetForm = () => {
    setForm({ iban: '', balance: '', customerId: '', productId: '', currency: 'USD' });
  };

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    createAccount(buildPayload()).then(() => {
      loadAccounts(page);
      resetForm();
    });
  };

  const handleUpdate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.iban.trim()) return;
    updateAccount(form.iban.trim(), buildPayload()).then(() => {
      loadAccounts(page);
      resetForm();
    });
  };

  const handleDelete = (iban: string) => {
    deleteAccount(iban).then(() => loadAccounts(page));
  };

  const handlePreviousPage = () => {
    if (page > 0) loadAccounts(page - 1);
  };

  const handleNextPage = () => {
    if (page < totalPages - 1) loadAccounts(page + 1);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">Accounts</h1>
        <p className="page-subtitle">Manage bank accounts and balances</p>
      </div>

      <div className="card">
        <h3 className="card-title">Create or Update Account</h3>
        <form onSubmit={handleCreate} className="form-grid">
          <div className="form-group">
            <label className="form-label">IBAN</label>
            <input name="iban" value={form.iban} onChange={handleChange} placeholder="AR1769751355549" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Balance</label>
            <input name="balance" value={form.balance} onChange={handleChange} placeholder="1000.00" type="number" step="0.01" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Customer ID</label>
            <input name="customerId" value={form.customerId} onChange={handleChange} placeholder="1" type="number" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Product ID</label>
            <input name="productId" value={form.productId} onChange={handleChange} placeholder="1" type="number" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Currency</label>
            <select name="currency" value={form.currency} onChange={handleChange} className="form-select" required>
              <option value="">Select currency</option>
              {CURRENCIES.map(curr => (
                <option key={curr} value={curr}>{curr}</option>
              ))}
            </select>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Create Account</button>
            <button type="button" onClick={handleUpdate} className="btn btn-secondary">Update Account</button>
          </div>
        </form>
      </div>

      <div className="filter-section">
        <div className="filter-group">
          <label className="form-label">Filter by Customer ID</label>
          <input
            value={customerFilter}
            onChange={e => setCustomerFilter(e.target.value)}
            placeholder="Enter customer ID"
            className="form-input"
          />
        </div>
        <button onClick={() => loadAccounts(0)} className="btn btn-primary">Apply Filter</button>
        <button onClick={() => { setCustomerFilter(''); loadAccounts(0); }} className="btn btn-outline">Clear Filter</button>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total Accounts</div>
          <div className="stat-value">{totalElements}</div>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>IBAN</th>
              <th>Balance</th>
              <th>Customer</th>
              <th>Currency</th>
              <th>Product</th>
              <th>Fee %</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map(a => (
              <tr key={a.accountId}>
                <td>{a.accountId}</td>
                <td><strong>{a.iban}</strong></td>
                <td><strong>${a.balance.toFixed(2)}</strong></td>
                <td>{a.customerId}</td>
                <td>{a.currency}</td>
                <td>{a.productName ?? '-'}</td>
                <td>{a.transactionFee ? `${(a.transactionFee * 100).toFixed(2)}%` : '-'}</td>
                <td>
                  <button onClick={() => handleDelete(a.iban)} className="btn btn-danger btn-sm">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {!customerFilter && (
        <div className="pagination">
          <div className="pagination-info">
            Showing page <span className="page-number">{page + 1}</span> of <span className="page-number">{totalPages || 1}</span>
          </div>
          <div className="pagination-controls">
            <button onClick={handlePreviousPage} disabled={page === 0} className="btn btn-outline btn-sm">Previous</button>
            <button onClick={handleNextPage} disabled={page >= totalPages - 1} className="btn btn-outline btn-sm">Next</button>
          </div>
        </div>
      )}
    </div>
  );
};
