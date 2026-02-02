import axiosInstance from './axiosInstance';

export const getCustomers = () => axiosInstance.get(import.meta.env.VITE_API_CUSTOMER_URL);
export const createCustomer = (data: any) => axiosInstance.post(import.meta.env.VITE_API_CUSTOMER_URL, data);
