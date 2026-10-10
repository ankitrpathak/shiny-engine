export type BookingStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface ServicePackage {
  id: number;
  title: string;
  description: string;
  price: number;
  durationMinutes: number;
  enabled: boolean;
  features?: string[];
}

export interface Booking {
  id: number;
  customerId: number;
  customerEmail: string;
  customerName: string;
  customerPhone: string;
  washerId?: number;
  washerName?: string;
  packageId: number;
  packageTitle: string;
  amount: number;
  vehicleModel: string;
  vehicleNumber: string;
  address: string;
  city: string;
  serviceDate: string;
  serviceTimeSlot: string;
  status: BookingStatus;
  paid: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookingRequest {
  packageId: number;
  vehicleModel: string;
  vehicleNumber: string;
  address: string;
  city: string;
  serviceDate: string;
  serviceTimeSlot: string;
  notes?: string;
}
