import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-employee-dashboard',
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './employee-dashboard.html',
  styleUrl: './employee-dashboard.css',
})
export class EmployeeDashboard {
  constructor(public authService: Auth, private router: Router) {}

  navigateToCustomers() {
    this.router.navigate(['/employee/customers']);
  }

  navigateToTrading() {
    // TODO: Trading Route später hinzufügen
    console.log('Trading noch nicht implementiert');
  }

  logout() {
    this.authService.logout();
  }
}
