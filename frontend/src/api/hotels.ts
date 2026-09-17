import { hotelClient } from './client'
import type { Hotel, HotelFormValues } from '../types/hotel'

export async function getHotel(id: string): Promise<Hotel> {
  const { data } = await hotelClient.get<Hotel>(`/api/hotels/${id}`)
  return data
}

// hotel-service has no "my hotels" endpoint -- GET /api/hotels returns every
// hotel system-wide, so callers filter client-side (see MyHotelsPage), same
// pattern as tour-service's getAllTours / business-service's getAllBusinesses.
export async function getAllHotels(): Promise<Hotel[]> {
  const { data } = await hotelClient.get<Hotel[]>('/api/hotels')
  return data
}

export async function createHotel(request: HotelFormValues): Promise<Hotel> {
  const { data } = await hotelClient.post<Hotel>('/api/hotels', request)
  return data
}

export async function updateHotel(id: string, request: HotelFormValues): Promise<Hotel> {
  const { data } = await hotelClient.put<Hotel>(`/api/hotels/${id}`, request)
  return data
}

// Admin moderation -- gated to the ADMIN role server-side.
export async function suspendHotel(id: string): Promise<Hotel> {
  const { data } = await hotelClient.patch<Hotel>(`/api/hotels/${id}/suspend`)
  return data
}

export async function reinstateHotel(id: string): Promise<Hotel> {
  const { data } = await hotelClient.patch<Hotel>(`/api/hotels/${id}/reinstate`)
  return data
}
