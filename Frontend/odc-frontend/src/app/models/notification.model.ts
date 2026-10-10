export type NotificationType =
  | 'BOOKING_CREATED'
  | 'BOOKING_ASSIGNED'
  | 'STATUS_UPDATE'
  | 'PAYMENT_SUCCESS'
  | 'ADMIN_ALERT';

export type NotificationStatus = 'UNREAD' | 'READ';

export interface Notification {
  id: number;
  userId: number;
  userEmail: string;
  title: string;
  message: string;
  type: NotificationType;
  status: NotificationStatus;
  referenceId?: number;
  createdAt: string;
}
