// Supported currencies across the application
export const CURRENCIES = [
  'ARS', // Argentine Peso
  'USD', // US Dollar
  'EUR', // Euro
  'GBP', // British Pound
  'BRL', // Brazilian Real
  'CLP', // Chilean Peso
  'MXN', // Mexican Peso
  'COP', // Colombian Peso
  'PEN', // Peruvian Sol
  'UYU', // Uruguayan Peso
] as const;

export type Currency = typeof CURRENCIES[number];

// Transaction types
export const TRANSACTION_TYPES = ['DEPOSIT', 'WITHDRAWAL'] as const;
export type TransactionType = typeof TRANSACTION_TYPES[number];

// Transaction statuses
export const TRANSACTION_STATUSES = ['PENDING', 'COMPLETED', 'REJECTED'] as const;
export type TransactionStatus = typeof TRANSACTION_STATUSES[number];

// Customer statuses
export const CUSTOMER_STATUSES = ['ACTIVE', 'INACTIVE', 'ERROR_ACCOUNT'] as const;
export type CustomerStatus = typeof CUSTOMER_STATUSES[number];
