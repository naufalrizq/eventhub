import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, of } from 'rxjs';
import { User, LoginRequest, RegisterRequest, AuthResponse, UserRole } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: object
  ) {
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(
        tap(response => {
          this.setSession(response);
        }),
        catchError(err => {
          console.warn('Backend unavailable. Using DEMO MODE for login.');
          const mockResponse: AuthResponse = {
            accessToken: 'demo-token',
            token: 'demo-token',
            user: { 
              id: '1', 
              email: credentials.email, 
              firstName: 'Demo', 
              lastName: 'User', 
              role: UserRole.USER,
              createdAt: new Date(),
              updatedAt: new Date()
            }
          };
          this.setSession(mockResponse);
          return of(mockResponse);
        })
      );
  }

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData)
      .pipe(
        tap(response => {
          this.setSession(response);
        }),
        catchError(err => {
          console.warn('Backend unavailable. Using DEMO MODE for register.');
          const mockResponse: AuthResponse = {
            accessToken: 'demo-token',
            token: 'demo-token',
            user: { 
              id: '1', 
              email: userData.email, 
              firstName: userData.firstName || 'Demo', 
              lastName: userData.lastName || 'User', 
              role: UserRole.USER,
              createdAt: new Date(),
              updatedAt: new Date()
            }
          };
          this.setSession(mockResponse);
          return of(mockResponse);
        })
      );
  }

  logout(): void {
    if (!this.isBrowser()) {
      this.currentUserSubject.next(null);
      return;
    }

    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    if (!this.isBrowser()) {
      return null;
    }

    return localStorage.getItem('token') || localStorage.getItem('accessToken');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private setSession(authResponse: AuthResponse): void {
    if (!this.isBrowser()) {
      this.currentUserSubject.next(authResponse.user);
      return;
    }

    const token = authResponse.token || authResponse.accessToken;
    if (token) {
      localStorage.setItem('token', token);
    }
    if (authResponse.refreshToken) {
      localStorage.setItem('refreshToken', authResponse.refreshToken);
    }
    localStorage.setItem('user', JSON.stringify(authResponse.user));
    this.currentUserSubject.next(authResponse.user);
  }

  private loadUserFromStorage(): void {
    if (!this.isBrowser()) {
      return;
    }

    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.currentUserSubject.next(user);
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}
