import React, { useEffect, useState } from 'react';
import {
  CreateTransactionRequest,
  Transaction,
  TransferRequest,
  createTransaction,
  listTransactions,
  transferTransaction,
} from '../services/transaction.service';
import { CURRENCIES } from '../constants';
import { useNotification } from '../context/NotificationContext';
import { getErrorMessage } from '../utils/errorHandler';

export const Transactions: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [accountFilter, setAccountFilter] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isTransferring, setIsTransferring] = useState(false);

  const [form, setForm] = useState({
    accountIban: '',
    amount: '',
    reference: '',
    currency: 'USD',
    type: 'DEPOSIT',
  });

  const [transferForm, setTransferForm] = useState({
    sourceIban: '',
    targetIban: '',
    amount: '',
    reference: '',
  });

  const loadTransactions = (pageNumber: number = 0) => {
    const params = {
      page: pageNumber,
      size: 10,
      ...(accountFilter.trim() ? { accountIban: accountFilter.trim() } : {}),
    };

    listTransactions(params)
      .then(res => {
        setTransactions(res.data.content);
        setPage(res.data.number);
        setTotalPages(res.data.totalPages);
        setTotalElements(res.data.totalElements);
      })
      .catch(error => {
        showError(`Failed to load transactions: ${getErrorMessage(error)}`);
      });
  };

  useEffect(() => {
    loadTransactions();
  }, []);

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleTransferChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setTransferForm({ ...transferForm, [e.target.name]: e.target.value });
  };

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;
    
    const payload: CreateTransactionRequest = {
      accountIban: form.accountIban.trim(),
      amount: Number(form.amount),
      reference: form.reference.trim() || undefined,
      currency: form.currency.trim(),
      type: form.type as CreateTransactionRequest['type'],
    };
    
    setIsSubmitting(true);
    createTransaction(payload)
      .then(() => {
        showSuccess(`${form.type === 'DEPOSIT' ? 'Deposit' : 'Withdrawal'} completed successfully!`);
        setForm({ accountIban: '', amount: '', reference: '', currency: 'USD', type: 'DEPOSIT' });
        loadTransactions(page);
      })
      .catch(error => {
        const errorMsg = getErrorMessage(error);
        showError(errorMsg);
      })
      .finally(() => {
        setIsSubmitting(false);
      });
  };

  const handleTransfer = (e: React.FormEvent) => {
    e.preventDefault();
    if (isTransferring) return;
    
    const payload: TransferRequest = {
      sourceIban: transferForm.sourceIban.trim(),
      targetIban: transferForm.targetIban.trim(),
      amount: Number(transferForm.amount),
      reference: transferForm.reference.trim() || undefined,
    };
    
    setIsTransferring(true);
    transferTransaction(payload)
      .then(() => {
        showSuccess('Transfer completed successfully!');
        setTransferForm({ sourceIban: '', targetIban: '', amount: '', reference: '' });
        loadTransactions(page);
      })
      .catch(error => {
        const errorMsg = getErrorMessage(error);
        showError(errorMsg);
      })
      .finally(() => {
        setIsTransferring(false);
      });
  };

  const handlePreviousPage = () => {
    if (page > 0) loadTransactions(page - 1);
  };

  const handleNextPage = () => {
    if (page < totalPages - 1) loadTransactions(page + 1);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">Transactions</h1>
        <p className="page-subtitle">Execute deposits, withdrawals, and transfers between accounts</p>
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 className="card-title">Create Transaction</h3>
          <form onSubmit={handleCreate} className="form-grid">
            <div className="form-group">
              <label className="form-label">Account IBAN</label>
              <input name="accountIban" value={form.accountIban} onChange={handleFormChange} placeholder="AR1769751355549" className="form-input" required />
            </div>
            <div className="form-group">
              <label className="form-label">Amount</label>
              <input name="amount" value={form.amount} onChange={handleFormChange} placeholder="100.00" type="number" step="0.01" className="form-input" required />
            </div>
            <div className="form-group">
              <label className="form-label">Reference (optional)</label>
              <input name="reference" value={form.reference} onChange={handleFormChange} placeholder="Salary payment" className="form-input" />
            </div>
            <div className="form-group">
              <label className="form-label">Currency</label>
              <select name="currency" value={form.currency} onChange={handleFormChange} className="form-select" required>
                <option value="">Select currency</option>
                {CURRENCIES.map(curr => (
                  <option key={curr} value={curr}>{curr}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Type</label>
              <select name="type" value={form.type} onChange={handleFormChange} className="form-select">
                <option value="DEPOSIT">Deposit</option>
                <option value="WITHDRAWAL">Withdrawal</option>
              </select>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                {isSubmitting ? 'Processing...' : 'Execute Transaction'}
              </button>
            </div>
          </form>
        </div>

        <div className="card">
          <h3 className="card-title">Transfer Between Accounts</h3>
          <form onSubmit={handleTransfer} className="form-grid">
            <div className="form-group">
              <label className="form-label">Source IBAN</label>
              <input name="sourceIban" value={transferForm.sourceIban} onChange={handleTransferChange} placeholder="AR1769751355549" className="form-input" required />
            </div>
            <div className="form-group">
              <label className="form-label">Target IBAN</label>
              <input name="targetIban" value={transferForm.targetIban} onChange={handleTransferChange} placeholder="AR1769751355550" className="form-input" required />
            </div>
            <div className="form-group">
              <label className="form-label">Amount</label>
              <input name="amount" value={transferForm.amount} onChange={handleTransferChange} placeholder="500.00" type="number" step="0.01" className="form-input" required />
            </div>
            <div className="form-group">
              <label className="form-label">Reference (optional)</label>
              <input name="reference" value={transferForm.reference} onChange={handleTransferChange} placeholder="Birthday gift" className="form-input" />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={isTransferring}>
                {isTransferring ? 'Processing...' : 'Transfer Funds'}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div className="filter-section">
        <div className="filter-group">
          <label className="form-label">Filter by Account IBAN</label>
          <input
            value={accountFilter}
            onChange={e => setAccountFilter(e.target.value)}
            placeholder="Enter account IBAN"
            className="form-input"
          />
        </div>
        <button onClick={() => loadTransactions(0)} className="btn btn-primary">Apply Filter</button>
        <button onClick={() => { setAccountFilter(''); loadTransactions(0); }} className="btn btn-outline">Clear Filter</button>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total Transactions</div>
          <div className="stat-value">{totalElements}</div>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Account</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Fee</th>
              <th>Total</th>
              <th>Currency</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map(t => (
              <tr key={t.transactionId}>
                <td>{t.transactionId}</td>
                <td><strong>{t.accountIban}</strong></td>
                <td>
                  <span className={`badge ${t.type === 'DEPOSIT' ? 'badge-success' : 'badge-warning'}`}>
                    {t.type}
                  </span>
                </td>
                <td><strong>${t.amount.toFixed(2)}</strong></td>
                <td>${t.fee.toFixed(2)}</td>
                <td>${t.total.toFixed(2)}</td>
                <td>{t.currency}</td>
                <td>
                  <span className={`badge ${
                    t.status === 'COMPLETED' ? 'badge-success' :
                    t.status === 'PENDING' ? 'badge-warning' : 'badge-danger'
                  }`}>
                    {t.status}
                  </span>
                </td>
                <td>{new Date(t.date).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="pagination">
        <div className="pagination-info">
          Showing page <span className="page-number">{page + 1}</span> of <span className="page-number">{totalPages || 1}</span>
        </div>
        <div className="pagination-controls">
          <button onClick={handlePreviousPage} disabled={page === 0} className="btn btn-outline btn-sm">Previous</button>
          <button onClick={handleNextPage} disabled={page >= totalPages - 1} className="btn btn-outline btn-sm">Next</button>
        </div>
      </div>
    </div>
  );
};
