import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Customer {
  private readonly API_URL = 'http://localhost:8080/ds-finance-bank-web/api';

  constructor(private http: HttpClient) {}

  async getAllCustomers(): Promise<any[]> {
    try {
      console.log('Fetching customers with HttpClient...');
      // HttpClient + Interceptor fügt automatisch Token hinzu!
      const customers = await lastValueFrom(
        this.http.get<any[]>(`${this.API_URL}/customers`)
      );
      console.log('Backend response:', customers);
      return customers;
    } catch (error: any) {
      console.error('Error fetching customers:', error);
      return [];
    }
  }

  async createCustomer(customerData: any): Promise<any> {
    try {
      return await lastValueFrom(
        this.http.post<any>(`${this.API_URL}/customers`, customerData)
      );
    } catch (error) {
      console.error('Error creating customer:', error);
      throw error;
    }
  }
  async deleteCustomer(customerNumber: string): Promise<void> {
    try {
      await lastValueFrom(
        this.http.delete<void>(`${this.API_URL}/customers/${customerNumber}`)
      );
    } catch (error) {
      console.error('Error deleting customer:', error);
      throw error;
    }
  }

  async updateCustomer(customerNumber: string, customerData: any): Promise<any> {
    try {
      return await lastValueFrom(
        this.http.put<any>(`${this.API_URL}/customers/${customerNumber}`, customerData)
      );
    } catch (error) {
      console.error('Error updating customer:', error);
      throw error;
    }
  }
}