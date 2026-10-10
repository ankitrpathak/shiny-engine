import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  fullName = '';
  email = '';
  password = '';
  phone = '';
  role: Role = 'CUSTOMER';
  address = '';
  city = '';

  loading = false;
  errorMessage = '';

  onSubmit(): void {
    if (!this.fullName || !this.email || !this.password || !this.phone) {
      this.errorMessage = 'Please complete all required fields.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService
      .register({
        fullName: this.fullName,
        email: this.email,
        password: this.password,
        phone: this.phone,
        role: this.role,
        address: this.address,
        city: this.city
      })
      .subscribe({
        next: (res) => {
          this.loading = false;
          if (res.role === 'WASHER') {
            this.router.navigate(['/washer']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage =
            err.error?.message || 'Registration failed. The email may already be in use.';
        }
      });
  }
}
