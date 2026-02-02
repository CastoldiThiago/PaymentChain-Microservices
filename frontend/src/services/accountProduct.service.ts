import axiosInstance from './axiosInstance';

export interface AccountProduct {
  id: number;
  name: string;
  transactionFeePercentage: number;
}

export interface CreateAccountProductRequest {
  name: string;
  transactionFeePercentage: number;
}

export const getAccountProducts = () =>
  axiosInstance.get<AccountProduct[]>(import.meta.env.VITE_API_ACCOUNT_PRODUCT_URL);

export const createAccountProduct = (data: CreateAccountProductRequest) =>
  axiosInstance.post(import.meta.env.VITE_API_ACCOUNT_PRODUCT_URL, data);
