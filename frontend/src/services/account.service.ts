import axiosInstance from './axiosInstance';

export const getAccountsByCustomer = (customerId: string) =>
  axiosInstance.get(`${import.meta.env.VITE_API_ACCOUNT_URL}/customer/${customerId}`);
export const createAccount = (data: any) => axiosInstance.post(import.meta.env.VITE_API_ACCOUNT_URL, data);
