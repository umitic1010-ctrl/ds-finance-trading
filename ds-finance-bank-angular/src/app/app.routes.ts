import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { EmployeeDashboard } from './pages/employee-dashboard/employee-dashboard';
import { CustomerDashboard } from './pages/customer-dashboard/customer-dashboard';
import { Customers } from './components/customers/customers';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'employee', component: EmployeeDashboard },
  { path: 'customer', component: CustomerDashboard },
  { path: 'employee/customers', component: Customers }
];
