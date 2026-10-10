export interface AdminDashboardStats {
  totalUsers: number;
  totalCustomers: number;
  totalWashers: number;
  activeUsers: number;
  inactiveUsers: number;
  totalBookings: number;
  pendingBookings: number;
  confirmedBookings: number;
  assignedBookings: number;
  inProgressBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  totalRevenue: number;
  totalPaymentsCount: number;
  successfulPaymentsCount: number;
  totalReviews: number;
  averageRating: number;
}

export interface AdminActionLog {
  id: number;
  adminId: number;
  adminEmail: string;
  actionType: string;
  targetResource: string;
  targetId: number;
  details: string;
  performedAt: string;
}
