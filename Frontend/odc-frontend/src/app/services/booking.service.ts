import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking, BookingStatus, CreateBookingRequest, ServicePackage } from '../models/booking.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private http = inject(HttpClient);
  private bookingApi = `${environment.apiUrl}/bookings`;
  private packageApi = `${environment.apiUrl}/packages`;

  getPackages(): Observable<ServicePackage[]> {
    return this.http.get<ServicePackage[]>(this.packageApi);
  }

  createBooking(request: CreateBookingRequest): Observable<Booking> {
    return this.http.post<Booking>(this.bookingApi, request);
  }

  getMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.bookingApi}/my-bookings`);
  }

  getMyAssignments(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.bookingApi}/my-assignments`);
  }

  getBookingById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.bookingApi}/${id}`);
  }

  updateBookingStatus(bookingId: number, status: BookingStatus): Observable<Booking> {
    return this.http.put<Booking>(`${this.bookingApi}/${bookingId}/status/${status}`, {});
  }
}
