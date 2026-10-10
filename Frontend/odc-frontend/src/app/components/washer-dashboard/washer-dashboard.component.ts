import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookingService } from '../../services/booking.service';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { Booking, BookingStatus } from '../../models/booking.model';
import { Review } from '../../models/review.model';

@Component({
  selector: 'app-washer-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './washer-dashboard.component.html',
  styleUrls: ['./washer-dashboard.component.css']
})
export class WasherDashboardComponent implements OnInit {
  private bookingService = inject(BookingService);
  private reviewService = inject(ReviewService);
  authService = inject(AuthService);

  assignments: Booking[] = [];
  reviews: Review[] = [];
  loading = true;
  alertMessage = '';
  alertType: 'success' | 'error' = 'success';

  ngOnInit(): void {
    this.loadAssignments();
    this.loadWasherReviews();
  }

  loadAssignments(): void {
    this.loading = true;
    this.bookingService.getMyAssignments().subscribe({
      next: (data) => {
        this.assignments = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadWasherReviews(): void {
    const user = this.authService.getCurrentUser();
    if (user?.userId) {
      this.reviewService.getReviewsByWasher(user.userId).subscribe({
        next: (data) => (this.reviews = data),
        error: () => {}
      });
    }
  }

  updateStatus(booking: Booking, newStatus: BookingStatus): void {
    this.bookingService.updateBookingStatus(booking.id, newStatus).subscribe({
      next: () => {
        this.showAlert(`Booking #${booking.id} status updated to ${newStatus}.`, 'success');
        this.loadAssignments();
      },
      error: (err) => {
        this.showAlert(err.error?.message || 'Could not update status.', 'error');
      }
    });
  }

  get assignedCount(): number {
    return this.assignments.filter((b) => b.status === 'ASSIGNED').length;
  }

  get inProgressCount(): number {
    return this.assignments.filter((b) => b.status === 'IN_PROGRESS').length;
  }

  get completedCount(): number {
    return this.assignments.filter((b) => b.status === 'COMPLETED').length;
  }

  get averageRating(): number {
    if (this.reviews.length === 0) return 5.0;
    const sum = this.reviews.reduce((acc, r) => acc + r.rating, 0);
    return Math.round((sum / this.reviews.length) * 10) / 10;
  }

  private showAlert(msg: string, type: 'success' | 'error'): void {
    this.alertMessage = msg;
    this.alertType = type;
    setTimeout(() => {
      this.alertMessage = '';
    }, 5000);
  }
}
