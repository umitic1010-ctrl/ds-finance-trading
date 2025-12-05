import apiClient, { createAuthClient } from './apiClient';
import { ENDPOINTS } from '../config/api';

// Bank Services
export const bankService = {
  initialize: () => apiClient.post(ENDPOINTS.BANK_INIT),
  getVolume: () => apiClient.get(ENDPOINTS.BANK_VOLUME)
};

// Customer Services
export const customerService = {
  create: (customer) => apiClient.post(ENDPOINTS.CUSTOMERS, customer),
  getAll: () => apiClient.get(ENDPOINTS.CUSTOMERS),
  search: (name) => apiClient.get(ENDPOINTS.CUSTOMER_SEARCH, { params: { name } }),
  getByNumber: (number) => apiClient.get(ENDPOINTS.CUSTOMER_BY_NUMBER(number))
};

// Trading Services
export const tradingService = {
  searchStocks: (query) => apiClient.get(ENDPOINTS.STOCKS_SEARCH, { params: { query } }),
  getDepot: (customerNumber) => apiClient.get(ENDPOINTS.DEPOT(customerNumber)),
  buyStocks: (data) => apiClient.post(ENDPOINTS.BUY_STOCKS, data),
  sellStocks: (data) => apiClient.post(ENDPOINTS.SELL_STOCKS, data)
};

export const authService = {
  validate: () => apiClient.get(ENDPOINTS.AUTH_ME).then((res) => res.data).catch(() => null),
  login: async (username, password) => {
    const client = createAuthClient(username, password);
    const { data } = await client.get(ENDPOINTS.AUTH_ME);
    return { username: data.username, role: data.role };
  },
};
