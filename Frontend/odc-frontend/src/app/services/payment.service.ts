import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RazorpayOrderResponse, VerifyPaymentRequest, PaymentRecord } from '../models/payment.model';
import { Booking } from '../models/booking.model';
import { environment } from '../../environments/environment';

declare var Razorpay: any;

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private http = inject(HttpClient);
  private paymentApi = `${environment.apiUrl}/payments`;

  createOrder(bookingId: number): Observable<RazorpayOrderResponse> {
    return this.http.post<RazorpayOrderResponse>(`${this.paymentApi}/create-order`, { bookingId });
  }

  verifyPayment(request: VerifyPaymentRequest): Observable<PaymentRecord> {
    return this.http.post<PaymentRecord>(`${this.paymentApi}/verify`, request);
  }

  getPaymentByBookingId(bookingId: number): Observable<PaymentRecord> {
    return this.http.get<PaymentRecord>(`${this.paymentApi}/booking/${bookingId}`);
  }

  loadRazorpayScript(): Promise<boolean> {
    return new Promise((resolve) => {
      if (typeof Razorpay !== 'undefined') {
        resolve(true);
        return;
      }
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  }

  openCheckout(
    order: RazorpayOrderResponse,
    booking: Booking,
    userEmail: string,
    onSuccess: (paymentId: string, orderId: string, signature: string) => void,
    onError: (error: any) => void
  ): void {
    const options = {
      key: order.keyId || environment.razorpayKeyId,
      amount: order.amount,
      currency: order.currency || 'INR',
      name: 'On Demand Car Wash',
      description: `Payment for ${booking.packageTitle} (#${booking.id})`,
      order_id: order.orderId,
      prefill: {
        name: booking.customerName,
        email: userEmail,
        contact: booking.customerPhone
      },
      theme: {
        color: '#2563eb'
      },
      handler: (response: any) => {
        onSuccess(
          response.razorpay_payment_id,
          response.razorpay_order_id,
          response.razorpay_signature
        );
      },
      modal: {
        ondismiss: () => {
          onError('Payment checkout closed by user');
        }
      }
    };

    if (typeof Razorpay !== 'undefined') {
      const rzp = new Razorpay(options);
      rzp.on('payment.failed', (resp: any) => {
        onError(resp.error);
      });
      rzp.open();
    } else {
      onError('Razorpay SDK failed to load');
    }
  }
}
