import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const TOKEN_KEY = 'jwt_token';
  const token = localStorage.getItem(TOKEN_KEY);
  
  // Token zu allen Requests hinzufügen (außer Login)
  if (token && !req.url.includes('/auth/login')) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  
  return next(req);
};
