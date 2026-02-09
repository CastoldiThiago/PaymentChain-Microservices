import axiosInstance from './axiosInstance';
import type { PageableResponse } from './customer.service';

export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL';

export interface Transaction {
  transactionId: number;
  accountIban: string;
  amount: number;
  fee: number;
  total: number;
  date: string;
  reference?: string;
  status: string;
  currency: string;
  customerId: number;
  type: TransactionType;
}

export interface CreateTransactionRequest {
  accountIban: string;
  amount: number;
  reference?: string;
  currency: string;
  type: TransactionType;
}

export interface TransferRequest {
  sourceIban: string;
  targetIban: string;
  amount: number;
  reference?: string;
}

interface GetTransactionsParams {
  page?: number;
  size?: number;
  sort?: string[];
  accountIban?: string;
}

export const listTransactions = (params: GetTransactionsParams = { page: 0, size: 10 }) =>
  axiosInstance.get<PageableResponse<Transaction>>(import.meta.env.VITE_API_TRANSACTION_URL, { params });

export const getTransactionById = (id: number) =>
  axiosInstance.get<Transaction>(`${import.meta.env.VITE_API_TRANSACTION_URL}/${id}`);

export const getTransactionsByAccount = (iban: string) =>
  axiosInstance.get<PageableResponse<Transaction>>(`${import.meta.env.VITE_API_TRANSACTION_URL}/account/${iban}`);

export const createTransaction = (data: CreateTransactionRequest, idempotencyKey?: string) =>
  axiosInstance.post(
    import.meta.env.VITE_API_TRANSACTION_URL,
    data,
    idempotencyKey ? { headers: { 'Idempotency-Key': idempotencyKey } } : undefined
  );

export const transferTransaction = (data: TransferRequest, idempotencyKey?: string) =>
  axiosInstance.post(
    `${import.meta.env.VITE_API_TRANSACTION_URL}/transfer`,
    data,
    idempotencyKey ? { headers: { 'Idempotency-Key': idempotencyKey } } : undefined
  );
