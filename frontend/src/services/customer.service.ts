import axiosInstance from './axiosInstance';

export interface Customer {
  customerId: number;
  name: string;
  surname: string;
  email: string;
  dni: string;
  status: string;
}

export interface PageableResponse<T> {
  totalElements: number;
  totalPages: number;
  pageable: {
    paged: boolean;
    pageSize: number;
    pageNumber: number;
    unpaged: boolean;
    offset: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
  };
  numberOfElements: number;
  size: number;
  content: T[];
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  first: boolean;
  last: boolean;
  empty: boolean;
}

interface GetCustomersParams {
  page?: number;
  size?: number;
  sort?: string[];
}

export const getCustomers = (params: GetCustomersParams = { page: 0, size: 10 }) => {
  return axiosInstance.get<PageableResponse<Customer>>(import.meta.env.VITE_API_CUSTOMER_URL, { params });
};

export const createCustomer = (data: any) => axiosInstance.post(import.meta.env.VITE_API_CUSTOMER_URL, data);
