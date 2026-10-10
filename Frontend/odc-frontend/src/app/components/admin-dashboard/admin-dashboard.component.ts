import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { AdminActionLog, AdminDashboardStats } from '../../models/admin.model';
import { Booking } from '../../models/booking.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  authService = inject(AuthService);

  activeTab: 'bookings' | 'users' | 'audit' = 'bookings';

  stats: AdminDashboardStats = {
    totalUsers: 0,
    totalCustomers: 0,
    totalWashers: 0,
    activeUsers: 0,
    inactiveUsers: 0,
    totalBookings: 0,
    pendingBookings: 0,
    confirmedBookings: 0,
    assignedBookings: 0,
    inProgressBookings: 0,
    completedBookings: 0,
    cancelledBookings: 0,
    totalRevenue: 0,
    totalPaymentsCount: 0,
    successfulPaymentsCount: 0,
    totalReviews: 0,
    averageRating: 5.0
  };

  bookings: Booking[] = [];
  users: User[] = [];
  auditLogs: AdminActionLog[] = [];
  washers: User[] = [];

  loadingStats = true;
  loadingData = true;

  // Assignment Modal
  showAssignModal = false;
  selectedBooking: Booking | null = null;
  selectedWasherId: number | null = null;
  assignSubmitting = false;

  alertMessage = '';
  alertType: 'success' | 'error' = 'success';

  ngOnInit(): void {
    this.loadStats();
    this.loadBookings();
    this.loadUsers();
    this.loadAuditLogs();
  }

  loadStats(): void {
    this.loadingStats = true;
    this.adminService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loadingStats = false;
      },
      error: () => {
        this.loadingStats = false;
      }
    });
  }

  loadBookings(): void {
    this.adminService.getAllBookings().subscribe({
      next: (data) => (this.bookings = data),
      error: () => {}
    });
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.washers = data.filter((u) => u.role === 'WASHER' && u.active);
      },
      error: () => {}
    });
  }

  loadAuditLogs(): void {
    this.adminService.getAuditLogs().subscribe({
      next: (data) => (this.auditLogs = data),
      error: () => {}
    });
  }

  // Washer Assignment
  openAssignModal(booking: Booking): void {
    this.selectedBooking = booking;
    this.selectedWasherId = this.washers.length > 0 ? this.washers[0].id : null;
    this.showAssignModal = true;
  }

  closeAssignModal(): void {
    this.showAssignModal = false;
    this.selectedBooking = null;
    this.selectedWasherId = null;
  }

  submitAssign(): void {
    if (!this.selectedBooking || !this.selectedWasherId) return;

    const washer = this.washers.find((w) => w.id === Number(this.selectedWasherId));
    const washerName = washer ? washer.fullName : 'Washer';

    this.assignSubmitting = true;
    this.adminService.assignWasher(this.selectedBooking.id, Number(this.selectedWasherId), washerName).subscribe({
      next: () => {
        this.assignSubmitting = false;
        this.closeAssignModal();
        this.showAlert(`Assigned ${washerName} to booking #${this.selectedBooking?.id}.`, 'success');
        this.loadBookings();
        this.loadStats();
        this.loadAuditLogs();
      },
      error: (err) => {
        this.assignSubmitting = false;
        this.showAlert(err.error?.message || 'Assignment failed.', 'error');
      }
    });
  }

  // User Moderation
  toggleBlockUser(user: User): void {
    if (user.role === 'GOD' || user.email === 'god@carwash.com') {
      this.showAlert('Root super-admin (GOD) account is immutable and cannot be blocked.', 'error');
      return;
    }

    if (user.active) {
      if (confirm(`Block user ${user.fullName} (${user.email})?`)) {
        this.adminService.blockUser(user.id, 'Administrative block').subscribe({
          next: () => {
            this.showAlert(`User ${user.email} blocked.`, 'success');
            this.loadUsers();
            this.loadAuditLogs();
          },
          error: (err) => this.showAlert(err.error?.message || 'Failed to block user.', 'error')
        });
      }
    } else {
      this.adminService.activateUser(user.id).subscribe({
        next: () => {
          this.showAlert(`User ${user.email} activated.`, 'success');
          this.loadUsers();
          this.loadAuditLogs();
        },
        error: (err) => this.showAlert(err.error?.message || 'Failed to activate user.', 'error')
      });
    }
  }

  promoteToAdmin(user: User): void {
    if (confirm(`Promote ${user.fullName} to ADMIN?`)) {
      this.adminService.promoteToAdmin(user.id).subscribe({
        next: () => {
          this.showAlert(`${user.fullName} promoted to ADMIN.`, 'success');
          this.loadUsers();
          this.loadAuditLogs();
        },
        error: (err) => this.showAlert(err.error?.message || 'Promotion failed.', 'error')
      });
    }
  }

  promoteToWasher(user: User): void {
    if (confirm(`Promote ${user.fullName} to WASHER?`)) {
      this.adminService.promoteToWasher(user.id).subscribe({
        next: () => {
          this.showAlert(`${user.fullName} promoted to WASHER.`, 'success');
          this.loadUsers();
          this.loadAuditLogs();
        },
        error: (err) => this.showAlert(err.error?.message || 'Promotion failed.', 'error')
      });
    }
  }

  demoteToCustomer(user: User): void {
    if (!this.authService.isGod()) {
      this.showAlert('Only super-admin (GOD) has permission to demote accounts.', 'error');
      return;
    }

    if (user.role === 'GOD') {
      this.showAlert('Cannot demote super-admin GOD account.', 'error');
      return;
    }

    if (confirm(`Demote ${user.fullName} to CUSTOMER?`)) {
      this.adminService.demoteToCustomer(user.id).subscribe({
        next: () => {
          this.showAlert(`${user.fullName} demoted to CUSTOMER.`, 'success');
          this.loadUsers();
          this.loadAuditLogs();
        },
        error: (err) => this.showAlert(err.error?.message || 'Demotion failed.', 'error')
      });
    }
  }

  private showAlert(msg: any, type: 'success' | 'error'): void {
    if (typeof msg === 'object' && msg !== null) {
      if (typeof msg.message === 'string') {
        this.alertMessage = msg.message;
      } else {
        this.alertMessage = Object.values(msg).join('; ');
      }
    } else {
      this.alertMessage = String(msg || '');
    }
    this.alertType = type;
    setTimeout(() => {
      this.alertMessage = '';
    }, 6000);
  }
}
