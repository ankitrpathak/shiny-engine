import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { PaymentService } from '../../services/payment.service';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { Booking, CreateBookingRequest, ServicePackage } from '../../models/booking.model';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-dashboard.component.html',
  styleUrls: ['./customer-dashboard.component.css']
})
export class CustomerDashboardComponent implements OnInit {
  private bookingService = inject(BookingService);
  private paymentService = inject(PaymentService);
  private reviewService = inject(ReviewService);
  authService = inject(AuthService);

  bookings: Booking[] = [];
  packages: ServicePackage[] = [];
  loading = true;

  // New Booking Modal State
  showBookingModal = false;
  bookingSubmitting = false;
  bookingForm: CreateBookingRequest = {
    packageId: 1,
    vehicleType: 'Sedan',
    vehicleModel: '',
    vehicleNumber: '',
    address: '',
    city: 'Bangalore',
    serviceAddress: '',
    serviceDate: '',
    serviceTimeSlot: '09:00 - 10:30',
    notes: '',
    customerNotes: ''
  };

  // Review Modal State
  showReviewModal = false;
  selectedBookingForReview: Booking | null = null;
  reviewRating = 5;
  reviewComment = '';
  reviewSubmitting = false;

  // Payment State
  paymentProcessing = false;
  alertMessage = '';
  alertType: 'success' | 'error' = 'success';

  ngOnInit(): void {
    this.loadPackages();
    this.loadMyBookings();
    this.paymentService.loadRazorpayScript();
  }

  loadPackages(): void {
    this.bookingService.getPackages().subscribe({
      next: (data) => (this.packages = data),
      error: () => {}
    });
  }

  loadMyBookings(): void {
    this.loading = true;
    this.bookingService.getMyBookings().subscribe({
      next: (data) => {
        this.bookings = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  openNewBooking(): void {
    const user = this.authService.getCurrentUser();
    // Default tomorrow's date
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateStr = tomorrow.toISOString().split('T')[0];

    this.bookingForm = {
      packageId: this.packages.length > 0 ? this.packages[0].id : 1,
      vehicleType: 'Sedan',
      vehicleModel: '',
      vehicleNumber: '',
      address: '',
      city: 'Bangalore',
      serviceAddress: '',
      serviceDate: dateStr,
      serviceTimeSlot: '09:00 - 10:30',
      notes: '',
      customerNotes: ''
    };
    this.showBookingModal = true;
  }

  closeBookingModal(): void {
    this.showBookingModal = false;
  }

  submitBooking(): void {
    if (!this.bookingForm.vehicleModel || !this.bookingForm.vehicleNumber || !this.bookingForm.address) {
      this.showAlert('Please fill in all vehicle and address details.', 'error');
      return;
    }

    const fullAddress = this.bookingForm.city
      ? `${this.bookingForm.address.trim()}, ${this.bookingForm.city.trim()}`
      : this.bookingForm.address.trim();

    const payload: CreateBookingRequest = {
      packageId: Number(this.bookingForm.packageId),
      vehicleType: this.bookingForm.vehicleType || 'Sedan',
      vehicleModel: this.bookingForm.vehicleModel.trim(),
      vehicleNumber: this.bookingForm.vehicleNumber.trim().toUpperCase(),
      serviceAddress: fullAddress,
      address: fullAddress,
      city: this.bookingForm.city,
      serviceDate: this.bookingForm.serviceDate,
      serviceTimeSlot: this.bookingForm.serviceTimeSlot,
      customerNotes: this.bookingForm.notes || '',
      notes: this.bookingForm.notes || ''
    };

    this.bookingSubmitting = true;
    this.bookingService.createBooking(payload).subscribe({
      next: () => {
        this.bookingSubmitting = false;
        this.closeBookingModal();
        this.showAlert('Booking placed successfully! You can pay now or after service.', 'success');
        this.loadMyBookings();
      },
      error: (err) => {
        this.bookingSubmitting = false;
        this.showAlert(this.formatErrorMessage(err, 'Failed to create booking.'), 'error');
      }
    });
  }

  // Razorpay Checkout Integration
  payNow(booking: Booking): void {
    this.paymentProcessing = true;
    const user = this.authService.getCurrentUser();
    const userEmail = user?.email || 'customer@carwash.com';

    this.paymentService.createOrder(booking.id).subscribe({
      next: (order) => {
        this.paymentService.openCheckout(
          order,
          booking,
          userEmail,
          (paymentId, orderId, signature) => {
            // Server-side verification
            this.paymentService
              .verifyPayment({
                bookingId: booking.id,
                razorpayOrderId: orderId,
                razorpayPaymentId: paymentId,
                razorpaySignature: signature
              })
              .subscribe({
                next: () => {
                  this.paymentProcessing = false;
                  this.showAlert(`Payment of ₹${booking.amount} verified! Booking marked as PAID.`, 'success');
                  this.loadMyBookings();
                },
                error: (err) => {
                  this.paymentProcessing = false;
                  this.showAlert(this.formatErrorMessage(err, 'Payment verification failed.'), 'error');
                }
              });
          },
          (err) => {
            this.paymentProcessing = false;
            this.showAlert(typeof err === 'string' ? err : 'Payment cancelled or failed.', 'error');
          }
        );
      },
      error: (err) => {
        this.paymentProcessing = false;
        this.showAlert(this.formatErrorMessage(err, 'Could not initiate Razorpay order.'), 'error');
      }
    });
  }

  // Review Modal
  openReview(booking: Booking): void {
    this.selectedBookingForReview = booking;
    this.reviewRating = 5;
    this.reviewComment = '';
    this.showReviewModal = true;
  }

  closeReviewModal(): void {
    this.showReviewModal = false;
    this.selectedBookingForReview = null;
  }

  submitReview(): void {
    if (!this.selectedBookingForReview) return;

    this.reviewSubmitting = true;
    this.reviewService
      .createReview({
        bookingId: this.selectedBookingForReview.id,
        rating: this.reviewRating,
        comment: this.reviewComment
      })
      .subscribe({
        next: () => {
          this.reviewSubmitting = false;
          this.closeReviewModal();
          this.showAlert('Thank you for rating your wash service!', 'success');
        },
        error: (err) => {
          this.reviewSubmitting = false;
          this.showAlert(this.formatErrorMessage(err, 'Failed to submit review.'), 'error');
        }
      });
  }

  cancelBooking(booking: Booking): void {
    if (confirm(`Are you sure you want to cancel booking #${booking.id}?`)) {
      this.bookingService.updateBookingStatus(booking.id, 'CANCELLED').subscribe({
        next: () => {
          this.showAlert(`Booking #${booking.id} cancelled.`, 'success');
          this.loadMyBookings();
        },
        error: (err) => this.showAlert(this.formatErrorMessage(err, 'Could not cancel booking.'), 'error')
      });
    }
  }

  private formatErrorMessage(err: any, fallback: string): string {
    if (!err) return fallback;
    const body = err.error;
    if (typeof body === 'string') return body;
    if (body) {
      if (typeof body.message === 'string') return body.message;
      if (typeof body.message === 'object' && body.message !== null) {
        return Object.values(body.message).join('; ');
      }
      if (typeof body.validationErrors === 'object' && body.validationErrors !== null) {
        return Object.values(body.validationErrors).join('; ');
      }
      if (typeof body.error === 'string') return body.error;
    }
    if (err.message && typeof err.message === 'string') return err.message;
    return fallback;
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
