import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { Notification } from '../../models/notification.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  notificationService = inject(NotificationService);
  router = inject(Router);

  unreadCount = 0;
  showNotifications = false;
  notifications: Notification[] = [];

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.loadNotifications();
      // Poll every 30 seconds for live updates
      setInterval(() => {
        if (this.authService.isLoggedIn()) {
          this.loadNotifications();
        }
      }, 30000);
    }
  }

  loadNotifications(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => (this.unreadCount = res.unreadCount),
      error: () => {}
    });
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.notificationService.getMyNotifications().subscribe({
        next: (data) => (this.notifications = data.slice(0, 8)),
        error: () => {}
      });
    }
  }

  markAsRead(n: Notification): void {
    this.notificationService.markAsRead(n.id).subscribe({
      next: () => {
        n.status = 'READ';
        if (this.unreadCount > 0) this.unreadCount--;
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.unreadCount = 0;
        this.notifications.forEach((n) => (n.status = 'READ'));
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.showNotifications = false;
    this.router.navigate(['/']);
  }
}
