import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Auth } from '../../services/auth';
import { Customer } from '../../services/customer/customer';
import { Router } from '@angular/router';
import { CustomerDialog } from '../customer-dialog/customer-dialog';

@Component({
  selector: 'app-customers',
  imports: [MatCardModule, MatButtonModule, MatTableModule, MatIconModule, MatDialogModule],
  templateUrl: './customers.html',
  styleUrl: './customers.css',
})
export class Customers implements OnInit {
  dataSource = new MatTableDataSource<any>([]);
  displayedColumns: string[] = ['customerNumber', 'firstName', 'lastName', 'email', 'city', 'actions'];

  constructor(
    public authService: Auth,
    private customerService: Customer,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    console.log('Customers ngOnInit called');
    this.loadCustomers();
  }

  async loadCustomers() {
    console.log('Loading customers...');
    try {
      const customers = await this.customerService.getAllCustomers();
      console.log('Customers loaded:', customers);
      this.dataSource.data = customers;
    } catch (error) {
      console.error('Error in loadCustomers:', error);
    }
  }
  async deleteCustomer(customer: any) {
    if (confirm(`Kunde ${customer.firstName} ${customer.lastName} wirklich löschen?`)) {
      try {
        await this.customerService.deleteCustomer(customer.customerNumber);
        console.log(`Customer ${customer.customerNumber} deleted.`);
        this.loadCustomers(); // Tabelle neu laden
      } catch (error) {
        console.error('Error deleting customer:', error);
        alert('Fehler beim Löschen!');
      }
    }
  }

  editCustomer(customer: any) {
    const dialogRef = this.dialog.open(CustomerDialog, {
      width: '600px',
      data: { customer, mode: 'edit' }
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        console.log('Customer updated, reloading list...');
        await this.loadCustomers();
      }
    });
  }

  createCustomer() {
    const dialogRef = this.dialog.open(CustomerDialog, {
      width: '600px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        console.log('Customer created, reloading list...');
        await this.loadCustomers();
      }
    });
  }

  goBack() {
    this.router.navigate(['/employee']);
  }
}
