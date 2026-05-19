import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/events',
    pathMatch: 'full'
  },
  {
    path: 'auth/login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'auth/register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'events',
    loadComponent: () => import('./features/events/event-list/event-list.component').then(m => m.EventListComponent)
  },
  {
    path: 'events/create',
    loadComponent: () => import('./features/events/event-create/event-create.component').then(m => m.EventCreateComponent),
    canActivate: [authGuard]
  },
  {
    path: 'events/:id',
    loadComponent: () => import('./features/events/event-detail/event-detail.component').then(m => m.EventDetailComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: '/events'
  }
];
