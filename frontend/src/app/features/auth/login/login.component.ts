import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Login to EventHub</mat-card-title>
        </mat-card-header>
        
        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" required>
              <mat-error *ngIf="loginForm.get('email')?.hasError('required')">
                Email is required
              </mat-error>
              <mat-error *ngIf="loginForm.get('email')?.hasError('email')">
                Enter a valid email
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput type="password" formControlName="password" required>
              <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                Password is required
              </mat-error>
            </mat-form-field>

            <button mat-raised-button color="primary" type="submit" 
                    [disabled]="loginForm.invalid || isLoading" class="full-width">
              {{ isLoading ? 'Logging in...' : 'Login' }}
            </button>
          </form>

          <div class="demo-credentials">
            <div class="demo-header">
              <span>🚀 Demo Account</span>
              <button mat-stroked-button color="accent" type="button" (click)="fillDemoCredentials()">
                Use Demo
              </button>
            </div>
            <p class="demo-detail">demo&#64;eventhub.com / demo123456</p>
          </div>
        </mat-card-content>

        <mat-card-actions>
          <p>Don't have an account? <a routerLink="/auth/register">Register here</a></p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 2rem;
    }

    .login-card {
      width: 100%;
      max-width: 420px;
    }

    .full-width {
      width: 100%;
      margin-bottom: 1rem;
    }

    .demo-credentials {
      margin-top: 1.25rem;
      padding: 1rem 1.25rem;
      background-color: #2c2c2c;
      border: 1px solid #444;
      border-radius: 8px;
      font-size: 0.875rem;
    }

    .demo-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.375rem;
      font-weight: 600;
      color: #ce93d8;
    }

    .demo-detail {
      margin: 0;
      color: #9e9e9e;
      font-size: 0.8rem;
    }

    mat-card-actions {
      text-align: center;
    }

    mat-card-actions a {
      color: #ce93d8;
    }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      const credentials: LoginRequest = this.loginForm.value;

      this.authService.login(credentials).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.snackBar.open('Login successful!', 'Close', { duration: 3000 });
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Login failed. Please check your credentials.', 'Close', { duration: 5000 });
        }
      });
    }
  }

  fillDemoCredentials(): void {
    this.loginForm.patchValue({
      email: 'demo@eventhub.com',
      password: 'demo123456'
    });
    this.onSubmit();
  }
}
