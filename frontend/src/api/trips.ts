import { tripClient } from './client'
import type { Trip } from '../types/trip'

export async function createTrip(bookingId: string): Promise<Trip> {
  const { data } = await tripClient.post<Trip>('/api/trips', { bookingId })
  return data
}

export async function getTrip(id: string): Promise<Trip> {
  const { data } = await tripClient.get<Trip>(`/api/trips/${id}`)
  return data
}

// Resolves the trip scheduled for a booking, if one exists yet -- 404 if
// the tourist hasn't scheduled one.
export async function getTripForBooking(bookingId: string): Promise<Trip> {
  const { data } = await tripClient.get<Trip>(`/api/trips/booking/${bookingId}`)
  return data
}

export async function getMyTrips(): Promise<Trip[]> {
  const { data } = await tripClient.get<Trip[]>('/api/trips/mine')
  return data
}

export async function startTrip(id: string): Promise<Trip> {
  const { data } = await tripClient.patch<Trip>(`/api/trips/${id}/start`)
  return data
}

export async function completeTrip(id: string): Promise<Trip> {
  const { data } = await tripClient.patch<Trip>(`/api/trips/${id}/complete`)
  return data
}

export async function cancelTrip(id: string): Promise<Trip> {
  const { data } = await tripClient.patch<Trip>(`/api/trips/${id}/cancel`)
  return data
}
