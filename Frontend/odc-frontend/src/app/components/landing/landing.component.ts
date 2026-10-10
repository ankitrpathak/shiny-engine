import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { ServicePackage } from '../../models/booking.model';
import { Review } from '../../models/review.model';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {
  private bookingService = inject(BookingService);
  private reviewService = inject(ReviewService);
  authService = inject(AuthService);
  private router = inject(Router);

  packages: ServicePackage[] = [];
  reviews: Review[] = [];
  loading = true;

  ngOnInit(): void {
    this.loadPackages();
    this.loadReviews();
  }

  loadPackages(): void {
    this.bookingService.getPackages().subscribe({
      next: (data) => {
        this.packages = data;
        this.loading = false;
      },
      error: () => {
        // Fallback default packages if service is loading
        this.packages = [
          { id: 1, title: 'Express Exterior Wash', description: 'Quick foam wash, high pressure rinse, and tire shine.', price: 399, durationMinutes: 30, enabled: true },
          { id: 2, title: 'Interior Deep Clean', description: 'Complete interior vacuum, dashboard polish, seat wiping.', price: 799, durationMinutes: 60, enabled: true },
          { id: 3, title: 'Full Premium Detailing', description: 'Exterior foam wash + interior detail + wax protection coat.', price: 1499, durationMinutes: 90, enabled: true },
          { id: 4, title: 'Ceramic Pro Hydrophobic Shield', description: 'Full detail + 3-month hydrophobic ceramic protection layer.', price: 2999, durationMinutes: 120, enabled: true }
        ];
        this.loading = false;
      }
    });
  }

  loadReviews(): void {
    this.reviewService.getAllReviews().subscribe({
      next: (data) => {
        this.reviews = data.slice(0, 6);
      },
      error: () => {}
    });
  }

  onSelectPackage(pkg: ServicePackage): void {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard'], { queryParams: { packageId: pkg.id } });
    } else {
      this.router.navigate(['/login'], { queryParams: { redirect: 'dashboard', packageId: pkg.id } });
    }
  }
}
