import axios, { AxiosError } from 'axios';

export interface ErrorResponse {
  message: string;
  status?: number;
  errors?: string[];
}

export const parseError = (error: unknown): ErrorResponse => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<any>;
    
    // Server responded with error
    if (axiosError.response) {
      const { status, data } = axiosError.response;
      
      // Extract error message from various backend formats
      let message = 'An error occurred';
      
      // Try to extract message from different formats
      if (typeof data === 'string') {
        message = data;
      } else if (data) {
        // Try different properties in order of priority
        message = data.detail          // BusinessRuleException format (PRIORITY!)
          || data.message 
          || data.error 
          || data.title              // Fallback to title if no detail
          || data.errorMessage
          || data.msg
          || (data.errors && Array.isArray(data.errors) ? data.errors.join(', ') : null)
          || message;
      }
      
      // Only use fallback messages if no specific message was found
      if (!data?.detail && !data?.message && !data?.error && !data?.errorMessage) {
        if (status === 404) {
          message = 'Resource not found';
        } else if (status === 400) {
          message = 'Invalid request';
        } else if (status === 409) {
          message = 'Resource already exists';
        } else if (status === 401) {
          message = 'Unauthorized - Please login again';
        } else if (status === 403) {
          message = 'Forbidden - You do not have permission';
        } else if (status === 500) {
          message = 'Internal server error';
        }
      }
      
      return {
        message,
        status,
        errors: data?.errors,
      };
    }
    
    // Network error
    if (axiosError.request) {
      return {
        message: 'Network error - Could not connect to server',
        status: 0,
      };
    }
  }
  
  // Generic error
  if (error instanceof Error) {
    return {
      message: error.message,
    };
  }
  
  return {
    message: 'An unexpected error occurred',
  };
};

export const getErrorMessage = (error: unknown): string => {
  const parsed = parseError(error);
  
  // Log error details for debugging (solo en desarrollo)
  if (import.meta.env.DEV) {
    console.group('🔍 Error Debug Info');
    console.log('📝 Parsed message:', parsed.message);
    console.log('📊 Status code:', parsed.status);
    if (axios.isAxiosError(error) && error.response) {
      console.log('📦 Backend response data:', error.response.data);
    }
    console.log('🔴 Full error:', error);
    console.groupEnd();
  }
  
  return parsed.message;
};

// Common error messages for specific business rules
export const ErrorMessages = {
  ACCOUNT_NOT_FOUND: 'Account not found',
  PRODUCT_NOT_FOUND: 'Account product not found',
  CUSTOMER_NOT_FOUND: 'Customer not found',
  INSUFFICIENT_FUNDS: 'Insufficient funds for this transaction',
  DUPLICATE_ACCOUNT: 'An account with this IBAN already exists',
  INVALID_AMOUNT: 'Invalid transaction amount',
  ACCOUNT_CREATION_FAILED: 'Failed to create account',
  TRANSACTION_FAILED: 'Transaction failed',
  TRANSFER_FAILED: 'Transfer failed',
};
