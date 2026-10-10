import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  email = '';
  password = '';
  loading = false;
  errorMessage = '';

  quickFill(role: string): void {
    if (role === 'god') {
      this.email = 'god@carwash.com';
      this.password = 'GodAdmin@2026!';
    } else if (role === 'admin') {
      this.email = 'admin@carwash.com';
      this.password = 'Admin@123';
    } else if (role === 'washer') {
      this.email = 'washer@carwash.com';
      this.password = 'Washer@123';
    } else if (role === 'customer') {
      this.email = 'customer@carwash.com';
      this.password = 'Customer@123';
    }
  }

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter both email and password.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        const redirect = this.route.snapshot.queryParams['redirect'];
        if (redirect) {
          this.router.navigate([`/${redirect}`]);
          return;
        }

        if (res.role === 'WASHER') {
          this.router.navigate(['/washer']);
        } else if (res.role === 'ADMIN' || res.role === 'GOD') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err.error?.message || 'Invalid credentials or user blocked. Please check your login details.';
      }
    });
  }
}
