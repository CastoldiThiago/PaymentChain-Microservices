import React, { useEffect, useState } from 'react';
import { getCustomers, createCustomer } from '../services/customer.service';

export const Customers: React.FC = () => {
  const [customers, setCustomers] = useState<any[]>([]);
  const [form, setForm] = useState({ name: '', email: '', dni: '' });

  useEffect(() => {
    getCustomers().then(res => setCustomers(res.data));
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createCustomer(form).then(() => {
      getCustomers().then(res => setCustomers(res.data));
      setForm({ name: '', email: '', dni: '' });
    });
  };

  return (
    <div>
      <h2>Customers</h2>
      <form onSubmit={handleSubmit}>
        <input name="name" value={form.name} onChange={handleChange} placeholder="Name" required />
        <input name="email" value={form.email} onChange={handleChange} placeholder="Email" required />
        <input name="dni" value={form.dni} onChange={handleChange} placeholder="DNI" required />
        <button type="submit">Create</button>
      </form>
      <ul>
        {customers.map(c => (
          <li key={c.id}>{c.name} - {c.email} - {c.dni}</li>
        ))}
      </ul>
    </div>
  );
};
