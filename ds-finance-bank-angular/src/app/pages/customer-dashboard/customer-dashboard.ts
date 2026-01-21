import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-customer-dashboard',
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './customer-dashboard.html',
  styleUrl: './customer-dashboard.css',
})
export class CustomerDashboard {
  constructor(public authService: Auth) {}

  logout() {
    this.authService.logout();
  }
}
