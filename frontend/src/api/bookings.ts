import { bookingClient } from './client'
import type { Booking, CreateBookingRequest } from '../types/booking'

export async function createBooking(request: CreateBookingRequest): Promise<Booking> {
  const { data } = await bookingClient.post<Booking>('/api/bookings', request)
  return data
}

export async function getMyBookings(): Promise<Booking[]> {
  const { data } = await bookingClient.get<Booking[]>('/api/bookings')
  return data
}

export async function getBooking(id: string): Promise<Booking> {
  const { data } = await bookingClient.get<Booking>(`/api/bookings/${id}`)
  return data
}

export async function cancelBooking(id: string): Promise<Booking> {
  const { data } = await bookingClient.patch<Booking>(`/api/bookings/${id}/cancel`)
  return data
}
