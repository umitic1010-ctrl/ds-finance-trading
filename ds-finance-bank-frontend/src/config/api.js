// Use a relative URL so the CRA dev-server proxy (see package.json) forwards requests to the backend
// This avoids CORS issues when developing on localhost:3000
export const API_BASE_URL = '/ds-finance-bank-web/api';

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
