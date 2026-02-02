import axiosInstance from './axiosInstance';

export const getTransactionsByAccount = (iban: string) =>
  axiosInstance.get(`${import.meta.env.VITE_API_TRANSACTION_URL}/account/${iban}`);
export const createTransaction = (data: any) => axiosInstance.post(import.meta.env.VITE_API_TRANSACTION_URL, data);
