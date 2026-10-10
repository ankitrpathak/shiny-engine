export interface Review {
  id: number;
  bookingId: number;
  customerId: number;
  customerEmail: string;
  customerName?: string;
  washerId?: number;
  rating: number; // 1-5
  comment: string;
  createdAt: string;
}

export interface CreateReviewRequest {
  bookingId: number;
  rating: number;
  comment: string;
}

export interface ReviewSummary {
  totalReviews: number;
  averageRating: number;
  ratingDistribution: { [key: number]: number };
}
