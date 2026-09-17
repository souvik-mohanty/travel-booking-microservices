// Mirrors backend/services/booking-service's BookingResponse record.
export type BookingStatus = 'PENDING' | 'PAID' | 'COMPLETED' | 'CANCELLED'

export interface Booking {
  id: string
  tourId: string
  userId: string
  numberOfParticipants: number
  totalPrice: number
  status: BookingStatus
  createdAt: string
  updatedAt: string
}

export interface CreateBookingRequest {
  tourId: string
  numberOfParticipants: number
}
