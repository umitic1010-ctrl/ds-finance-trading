import { Component, inject, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Customer as CustomerService } from '../../services/customer/customer';

export interface CustomerDialogData {
  customer?: any;
  mode: 'create' | 'edit';
}

@Component({
  selector: 'app-customer-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './customer-dialog.html',
  styleUrl: './customer-dialog.css'
})
export class CustomerDialog {
  private fb = inject(FormBuilder);
  private customerService = inject(CustomerService);
  private dialogRef = inject(MatDialogRef<CustomerDialog>);
  
  customerForm: FormGroup;
  mode: 'create' | 'edit';
  title: string;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerDialogData) {
    this.mode = data.mode;
    this.title = this.mode === 'create' ? 'Neuen Kunden anlegen' : 'Kunde bearbeiten';
    
    // Form initialisieren
    this.customerForm = this.fb.group({
      firstName: [data.customer?.firstName || '', [Validators.required, Validators.minLength(2)]],
      lastName: [data.customer?.lastName || '', [Validators.required, Validators.minLength(2)]],
      email: [data.customer?.email || '', [Validators.required, Validators.email]],
      address: [data.customer?.address || '', Validators.required],
      city: [data.customer?.city || '', Validators.required],
      postalCode: [data.customer?.postalCode || '', Validators.required],
      country: [data.customer?.country || 'Austria', Validators.required],
      phoneNumber: [data.customer?.phoneNumber || '', Validators.required]
    });
  }

  async onSubmit() {
    if (this.customerForm.invalid) {
      this.customerForm.markAllAsTouched();
      return;
    }

    try {
      const formData = this.customerForm.value;

      if (this.mode === 'create') {
        console.log('Creating customer:', formData);
        await this.customerService.createCustomer(formData);
      } else {
        console.log('Updating customer:', this.data.customer.customerNumber, formData);
        await this.customerService.updateCustomer(this.data.customer.customerNumber, formData);
      }

      this.dialogRef.close(true); // Dialog schließen mit Erfolg
    } catch (error) {
      console.error('Error saving customer:', error);
      alert(`Fehler beim Speichern: ${error}`);
    }
  }

  onCancel() {
    this.dialogRef.close(false);
  }

  getErrorMessage(fieldName: string): string {
    const control = this.customerForm.get(fieldName);
    
    if (control?.hasError('required')) {
      return 'Dieses Feld ist erforderlich';
    }
    
    if (control?.hasError('email')) {
      return 'Ungültige E-Mail-Adresse';
    }
    
    if (control?.hasError('minlength')) {
      const minLength = control.getError('minlength').requiredLength;
      return `Mindestens ${minLength} Zeichen erforderlich`;
    }
    
    return '';
  }
}
