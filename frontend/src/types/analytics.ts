// Mirrors backend/services/analytics-service's response records.
export interface BookingSummary {
  totalBookings: number
  totalRevenue: number
  currency: string
}

export interface DailyBookingStats {
  date: string
  bookingCount: number
  revenue: number
}

export interface TourBookingStats {
  tourId: string
  bookingCount: number
  revenue: number
}
