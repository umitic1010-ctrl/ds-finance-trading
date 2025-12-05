export const API_BASE_URL = 'http://localhost:8080/ds-finance-bank-web/api';

export const ENDPOINTS = {
  // Bank
  BANK_INIT: '/bank/init',
  BANK_VOLUME: '/bank/volume',

  // Customers
  CUSTOMERS: '/customers',
  CUSTOMER_SEARCH: '/customers/search',
  CUSTOMER_BY_NUMBER: (number) => `/customers/${number}`,

  // Trading
  STOCKS_SEARCH: '/trading/stocks/search',
  DEPOT: (customerNumber) => `/trading/depot/${customerNumber}`,
  BUY_STOCKS: '/trading/buy',
  SELL_STOCKS: '/trading/sell',

  // Auth
  AUTH_ME: '/auth/me',
};
