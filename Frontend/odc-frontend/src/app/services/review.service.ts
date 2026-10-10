import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateReviewRequest, Review, ReviewSummary } from '../models/review.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private http = inject(HttpClient);
  private reviewApi = `${environment.apiUrl}/reviews`;

  createReview(request: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(this.reviewApi, request);
  }

  getMyReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.reviewApi}/my-reviews`);
  }

  getReviewsByWasher(washerId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.reviewApi}/washer/${washerId}`);
  }

  getAllReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(this.reviewApi);
  }

  getReviewByBookingId(bookingId: number): Observable<Review> {
    return this.http.get<Review>(`${this.reviewApi}/booking/${bookingId}`);
  }

  getReviewSummary(): Observable<ReviewSummary> {
    return this.http.get<ReviewSummary>(`${this.reviewApi}/admin/summary`);
  }
}
