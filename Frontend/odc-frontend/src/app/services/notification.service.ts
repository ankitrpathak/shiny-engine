import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../models/notification.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private notifApi = `${environment.apiUrl}/notifications`;

  getMyNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.notifApi);
  }

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.notifApi}/unread`);
  }

  getUnreadCount(): Observable<{ userId: number; unreadCount: number }> {
    return this.http.get<{ userId: number; unreadCount: number }>(`${this.notifApi}/unread/count`);
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.notifApi}/${id}/read`, {});
  }

  markAllAsRead(): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.notifApi}/read-all`, {});
  }
}
