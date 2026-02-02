import axiosInstance from './axiosInstance';
import type { PageableResponse } from './customer.service';

export interface Account {
  accountId: number;
  iban: string;
  balance: number;
  customerId: number;
  currency: string;
  productName?: string;
  transactionFee?: number;
}

export interface CreateAccountRequest {
  iban: string;
  balance: number;
  customerId: number;
  productId: number;
  currency: string;
}

interface GetAccountsParams {
  page?: number;
  size?: number;
  sort?: string[];
}

export const getAccounts = (params: GetAccountsParams = { page: 0, size: 10 }) =>
  axiosInstance.get<PageableResponse<Account>>(import.meta.env.VITE_API_ACCOUNT_URL, { params });

export const getAccountByIban = (iban: string) =>
  axiosInstance.get<Account>(`${import.meta.env.VITE_API_ACCOUNT_URL}/${iban}`);

export const getAccountsByCustomer = (customerId: string) =>
  axiosInstance.get<Account[]>(`${import.meta.env.VITE_API_ACCOUNT_URL}/customer/${customerId}`);

export const createAccount = (data: CreateAccountRequest) =>
  axiosInstance.post(import.meta.env.VITE_API_ACCOUNT_URL, data);

export const updateAccount = (iban: string, data: CreateAccountRequest) =>
  axiosInstance.put(`${import.meta.env.VITE_API_ACCOUNT_URL}/${iban}`, data);

export const deleteAccount = (iban: string) =>
  axiosInstance.delete(`${import.meta.env.VITE_API_ACCOUNT_URL}/${iban}`);
