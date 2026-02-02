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
    <div>
      <h2>Customers</h2>
      <form onSubmit={handleSubmit}>
        <input name="name" value={form.name} onChange={handleChange} placeholder="Name" required />
        <input name="surname" value={form.surname} onChange={handleChange} placeholder="Surname" required />
        <input name="email" value={form.email} onChange={handleChange} placeholder="Email" required />
        <input name="dni" value={form.dni} onChange={handleChange} placeholder="DNI" required />
        <input name="phone" value={form.phone} onChange={handleChange} placeholder="Phone" required />
        <button type="submit">Create</button>
      </form>
      
      <div>
        <p>Total: {totalElements} customers</p>
      </div>
      
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={{ border: '1px solid #ddd', padding: '8px' }}>ID</th>
            <th style={{ border: '1px solid #ddd', padding: '8px' }}>Name</th>
            <th style={{ border: '1px solid #ddd', padding: '8px' }}>Surname</th>
            <th style={{ border: '1px solid #ddd', padding: '8px' }}>Email</th>
            <th style={{ border: '1px solid #ddd', padding: '8px' }}>DNI</th>
            <th style={{ border: '1px solid #ddd', padding: '8px' }}>Status</th>
          </tr>
        </thead>
        <tbody>
          {customers.map(c => (
            <tr key={c.customerId}>
              <td style={{ border: '1px solid #ddd', padding: '8px' }}>{c.customerId}</td>
              <td style={{ border: '1px solid #ddd', padding: '8px' }}>{c.name}</td>
              <td style={{ border: '1px solid #ddd', padding: '8px' }}>{c.surname}</td>
              <td style={{ border: '1px solid #ddd', padding: '8px' }}>{c.email}</td>
              <td style={{ border: '1px solid #ddd', padding: '8px' }}>{c.dni}</td>
              <td style={{ border: '1px solid #ddd', padding: '8px' }}>{c.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
      
      <div style={{ marginTop: '20px', display: 'flex', gap: '10px', alignItems: 'center' }}>
        <button onClick={handlePreviousPage} disabled={page === 0}>Previous</button>
        <span>Page {page + 1} of {totalPages || 1}</span>
        <button onClick={handleNextPage} disabled={page >= totalPages - 1}>Next</button>
      </div>
    </div>
  );
};
