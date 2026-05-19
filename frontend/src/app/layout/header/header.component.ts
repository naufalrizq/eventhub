import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { Observable } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule
  ],
  template: `
    <mat-toolbar color="primary">
      <span routerLink="/" class="logo">EventHub</span>
      
      <div class="nav-links">
        <a mat-button routerLink="/events">Events</a>
        <a mat-button routerLink="/dashboard" *ngIf="currentUser$ | async">Dashboard</a>
      </div>

      <div class="spacer"></div>

      <div *ngIf="currentUser$ | async as user; else loginButtons" class="user-menu">
        <button mat-button [matMenuTriggerFor]="userMenu">
          <mat-icon>account_circle</mat-icon>
          {{ user.firstName }}
        </button>
        <mat-menu #userMenu="matMenu">
          <button mat-menu-item routerLink="/profile">
            <mat-icon>person</mat-icon>
            Profile
          </button>
          <button mat-menu-item routerLink="/dashboard">
            <mat-icon>event</mat-icon>
            My Events
          </button>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon>
            Logout
          </button>
        </mat-menu>
      </div>

      <ng-template #loginButtons>
        <a mat-button routerLink="/auth/login">Login</a>
        <a mat-raised-button color="accent" routerLink="/auth/register">Register</a>
      </ng-template>
    </mat-toolbar>
  `,
  styles: [`
    .logo {
      font-size: 1.5rem;
      font-weight: bold;
      cursor: pointer;
      text-decoration: none;
      color: inherit;
    }

    .nav-links {
      margin-left: 2rem;
    }

    .spacer {
      flex: 1 1 auto;
    }

    .user-menu {
      display: flex;
      align-items: center;
    }
  `]
})
export class HeaderComponent implements OnInit {
  currentUser$: Observable<User | null>;

  constructor(private authService: AuthService) {
    this.currentUser$ = this.authService.currentUser$;
  }

  ngOnInit(): void {}

  logout(): void {
    this.authService.logout();
  }
}
