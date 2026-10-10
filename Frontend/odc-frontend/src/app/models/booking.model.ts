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
  vehicleType?: string;
  vehicleModel: string;
  vehicleNumber: string;
  serviceAddress?: string;
  address?: string;
  city?: string;
  serviceDate: string;
  serviceTimeSlot: string;
  status: BookingStatus;
  paid: boolean;
  customerNotes?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookingRequest {
  packageId: number;
  vehicleType?: string;
  vehicleModel: string;
  vehicleNumber: string;
  serviceAddress?: string;
  address?: string;
  city?: string;
  serviceDate: string;
  serviceTimeSlot: string;
  customerNotes?: string;
  notes?: string;
}
