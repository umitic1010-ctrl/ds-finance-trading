import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';

export interface User {
  id: number;
  email: string;
  role: string;
  firstName: string;
  lastName: string;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly API_URL = 'http://localhost:8080/ds-finance-bank-web/api';

  // Signal für reaktiven State (ähnlich wie useState in React)
  currentUser = signal<User | null>(null);
  isAuthenticated = signal<boolean>(false);

  constructor(private router: Router, private http: HttpClient) {
    // Beim Start prüfen ob Token existiert
    this.checkAuth();
  }

  private checkAuth() {
    const token = this.getToken();
    if (token) {
      // Hier könntest du den Token validieren
      // Für jetzt laden wir den User aus dem Token
      try {
        const payload = this.decodeToken(token);
        this.currentUser.set(payload.user);
        this.isAuthenticated.set(true);
      } catch (error) {
        this.logout();
      }
    }
  }

  async login(email: string, password: string): Promise<void> {
    try {
      // HttpClient gibt Observable zurück, lastValueFrom macht Promise daraus
      const response = await lastValueFrom(
        this.http.post<any>(`${this.API_URL}/auth/login`, { email, password })
      );

      // Backend gibt zurück: {token, role, personId, email, customerId, customerNumber}
      const { token, role, personId, email: userEmail, customerId, customerNumber } = response;
      
      // Token speichern
      localStorage.setItem(this.TOKEN_KEY, token);
      
      // User Object erstellen
      const user: User = {
        id: personId,
        email: userEmail,
        role: role,
        firstName: '', // Backend gibt keine Namen zurück
        lastName: ''
      };
      
      // User State aktualisieren
      this.currentUser.set(user);
      this.isAuthenticated.set(true);

      // Zur passenden Dashboard navigieren
      if (role === 'employee') {
        this.router.navigate(['/employee']);
      } else {
        this.router.navigate(['/customer']);
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || error.response?.data?.message || 'Login fehlgeschlagen';
      throw new Error(errorMessage);
    }
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error('Invalid token');
    }
  }
}
