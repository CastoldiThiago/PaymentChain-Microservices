import React, { useEffect, useState } from 'react';
import { getCustomers, createCustomer, Customer } from '../services/customer.service';

export const Customers: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [form, setForm] = useState({ name: '', surname: '', email: '', dni: '', phone: '' });

  const loadCustomers = (pageNumber: number = 0) => {
    getCustomers({ page: pageNumber, size: 10 }).then(res => {
      setCustomers(res.data.content);
      setPage(res.data.number);
      setTotalPages(res.data.totalPages);
      setTotalElements(res.data.totalElements);
    });
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createCustomer(form).then(() => {
      loadCustomers(page);
      setForm({ name: '', surname: '', email: '', dni: '', phone: '' });
    });
  };

  const handlePreviousPage = () => {
    if (page > 0) {
      loadCustomers(page - 1);
    }
  };

  const handleNextPage = () => {
    if (page < totalPages - 1) {
      loadCustomers(page + 1);
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">Customers</h1>
        <p className="page-subtitle">Manage customer information and create new accounts</p>
      </div>

      <div className="card">
        <h3 className="card-title">Create New Customer</h3>
        <form onSubmit={handleSubmit} className="form-grid">
          <div className="form-group">
            <label className="form-label">Name</label>
            <input name="name" value={form.name} onChange={handleChange} placeholder="John" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Surname</label>
            <input name="surname" value={form.surname} onChange={handleChange} placeholder="Doe" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Email</label>
            <input name="email" value={form.email} onChange={handleChange} placeholder="john.doe@example.com" type="email" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">DNI</label>
            <input name="dni" value={form.dni} onChange={handleChange} placeholder="12345678" className="form-input" required />
          </div>
          <div className="form-group">
            <label className="form-label">Phone</label>
            <input name="phone" value={form.phone} onChange={handleChange} placeholder="+1234567890" className="form-input" required />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Create Customer</button>
          </div>
        </form>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total Customers</div>
          <div className="stat-value">{totalElements}</div>
        </div>
      </div>
      
      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Surname</th>
              <th>Email</th>
              <th>DNI</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {customers.map(c => (
              <tr key={c.customerId}>
                <td>{c.customerId}</td>
                <td>{c.name}</td>
                <td>{c.surname}</td>
                <td>{c.email}</td>
                <td>{c.dni}</td>
                <td>
                  <span className={`badge ${c.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}`}>
                    {c.status}
                  </span>
                </td>
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
