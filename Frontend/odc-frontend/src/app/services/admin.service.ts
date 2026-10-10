import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminActionLog, AdminDashboardStats } from '../models/admin.model';
import { User } from '../models/user.model';
import { Booking } from '../models/booking.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private adminApi = `${environment.apiUrl}/admin`;

  getDashboardStats(): Observable<AdminDashboardStats> {
    return this.http.get<AdminDashboardStats>(`${this.adminApi}/dashboard/stats`);
  }

  getAuditLogs(): Observable<AdminActionLog[]> {
    return this.http.get<AdminActionLog[]>(`${this.adminApi}/audit-logs`);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.adminApi}/users`);
  }

  blockUser(userId: number, reason?: string): Observable<User> {
    return this.http.put<User>(`${this.adminApi}/users/${userId}/block`, { active: false, reason });
  }

  activateUser(userId: number): Observable<User> {
    return this.http.put<User>(`${this.adminApi}/users/${userId}/activate`, {});
  }

  promoteToAdmin(userId: number): Observable<User> {
    return this.http.put<User>(`${this.adminApi}/users/${userId}/promote-admin`, {});
  }

  promoteToWasher(userId: number): Observable<User> {
    return this.http.put<User>(`${this.adminApi}/users/${userId}/promote-washer`, {});
  }

  demoteToCustomer(userId: number): Observable<User> {
    return this.http.put<User>(`${this.adminApi}/users/${userId}/demote-customer`, {});
  }

  getAllBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.adminApi}/bookings`);
  }

  assignWasher(bookingId: number, washerId: number, washerName: string): Observable<Booking> {
    return this.http.put<Booking>(`${this.adminApi}/bookings/${bookingId}/assign`, {
      washerId,
      washerName
    });
  }

  getAllPayments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminApi}/payments`);
  }

  getAllReviews(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminApi}/reviews`);
  }
}
