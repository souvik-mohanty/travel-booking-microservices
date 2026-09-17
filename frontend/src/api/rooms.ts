import { hotelClient } from './client'
import type { Room, RoomAvailability, RoomFormValues, RoomReservation } from '../types/hotel'

export async function getRoom(id: string): Promise<Room> {
  const { data } = await hotelClient.get<Room>(`/api/rooms/${id}`)
  return data
}

export async function getRoomsForHotel(hotelId: string): Promise<Room[]> {
  const { data } = await hotelClient.get<Room[]>(`/api/hotels/${hotelId}/rooms`)
  return data
}

// Public search: only rooms a tour is allowed to book.
export async function getActiveRooms(): Promise<Room[]> {
  const { data } = await hotelClient.get<Room[]>('/api/rooms/active')
  return data
}

export async function createRoom(hotelId: string, request: RoomFormValues): Promise<Room> {
  const { data } = await hotelClient.post<Room>('/api/rooms', { ...request, hotelId })
  return data
}

export async function updateRoom(id: string, request: RoomFormValues): Promise<Room> {
  const { data } = await hotelClient.put<Room>(`/api/rooms/${id}`, request)
  return data
}

export async function activateRoom(id: string): Promise<Room> {
  const { data } = await hotelClient.patch<Room>(`/api/rooms/${id}/activate`)
  return data
}

export async function deactivateRoom(id: string): Promise<Room> {
  const { data } = await hotelClient.patch<Room>(`/api/rooms/${id}/deactivate`)
  return data
}

export async function deleteRoom(id: string): Promise<Room> {
  const { data } = await hotelClient.delete<Room>(`/api/rooms/${id}`)
  return data
}

export async function getRoomAvailability(
  roomId: string,
  startDate: string,
  endDate: string,
): Promise<RoomAvailability> {
  const { data } = await hotelClient.get<RoomAvailability>(`/api/rooms/${roomId}/availability`, {
    params: { startDate, endDate },
  })
  return data
}

// Hotel-owner visibility: which tours have reserved this room.
export async function getReservationsForRoom(roomId: string): Promise<RoomReservation[]> {
  const { data } = await hotelClient.get<RoomReservation[]>(`/api/rooms/${roomId}/reservations`)
  return data
}
