import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, Role, User } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/auth`;
  private userApiUrl = `${environment.apiUrl}/users`;

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((res) => {
        this.saveAuth(res);
      })
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((res) => {
        this.saveAuth(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('odc_token');
    localStorage.removeItem('odc_user');
    this.currentUserSubject.next(null);
  }

  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.userApiUrl}/me`);
  }

  getToken(): string | null {
    return localStorage.getItem('odc_token');
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getRole(): Role | null {
    const user = this.getCurrentUser();
    return user ? user.role : null;
  }

  isCustomer(): boolean {
    return this.getRole() === 'CUSTOMER';
  }

  isWasher(): boolean {
    return this.getRole() === 'WASHER';
  }

  isAdmin(): boolean {
    const role = this.getRole();
    return role === 'ADMIN' || role === 'GOD';
  }

  isGod(): boolean {
    return this.getRole() === 'GOD';
  }

  private saveAuth(auth: AuthResponse): void {
    localStorage.setItem('odc_token', auth.token);
    localStorage.setItem('odc_user', JSON.stringify(auth));
    this.currentUserSubject.next(auth);
  }

  private getStoredUser(): AuthResponse | null {
    const userStr = localStorage.getItem('odc_user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }
}
