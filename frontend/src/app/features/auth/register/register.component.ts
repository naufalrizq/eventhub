import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest, UserRole } from '../../../core/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  template: `
    <div class="register-container">
      <mat-card class="register-card">
        <mat-card-header>
          <mat-card-title>Create your EventHub account</mat-card-title>
          <mat-card-subtitle>Join events or manage your own event program.</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>First name</mat-label>
                <input matInput formControlName="firstName" required>
                <mat-error *ngIf="registerForm.get('firstName')?.hasError('required')">First name is required</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Last name</mat-label>
                <input matInput formControlName="lastName" required>
                <mat-error *ngIf="registerForm.get('lastName')?.hasError('required')">Last name is required</mat-error>
              </mat-form-field>
            </div>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" required>
              <mat-error *ngIf="registerForm.get('email')?.hasError('required')">Email is required</mat-error>
              <mat-error *ngIf="registerForm.get('email')?.hasError('email')">Enter a valid email</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput type="password" formControlName="password" required>
              <mat-error *ngIf="registerForm.get('password')?.hasError('required')">Password is required</mat-error>
              <mat-error *ngIf="registerForm.get('password')?.hasError('minlength')">Use at least 8 characters</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Account type</mat-label>
              <mat-select formControlName="role">
                <mat-option [value]="UserRole.USER">Attendee</mat-option>
                <mat-option [value]="UserRole.ORGANIZER">Organizer</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-raised-button color="primary" type="submit" [disabled]="registerForm.invalid || isLoading" class="full-width">
              <mat-icon>person_add</mat-icon>
              {{ isLoading ? 'Creating account...' : 'Create Account' }}
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <p>Already have an account? <a routerLink="/auth/login">Login here</a></p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 2rem;
    }

    .register-card {
      width: 100%;
      max-width: 520px;
    }

    .form-row {
      display: flex;
      gap: 1rem;
    }

    .form-row mat-form-field {
      flex: 1;
    }

    .full-width {
      width: 100%;
      margin-bottom: 1rem;
    }

    button.full-width {
      display: flex;
      justify-content: center;
      gap: 0.5rem;
    }

    mat-card-actions {
      justify-content: center;
      text-align: center;
    }

    @media (max-width: 600px) {
      .form-row {
        flex-direction: column;
        gap: 0;
      }
    }
  `]
})
export class RegisterComponent {
  readonly UserRole = UserRole;
  registerForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      role: [UserRole.USER]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;
    const userData: RegisterRequest = this.registerForm.value;

    this.authService.register(userData).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('Account created successfully!', 'Close', { duration: 3000 });
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Registration failed. Please check your details.', 'Close', { duration: 5000 });
      }
    });
  }
}
