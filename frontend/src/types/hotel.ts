// Mirrors backend/services/hotel-service's Hotel/Room/RoomReservation responses.
export type HotelStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE'
export type RoomStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'DELETED'
export type ReservationStatus = 'ACTIVE' | 'CANCELLED'

export interface Hotel {
  id: string
  ownerId: string
  name: string
  description: string | null
  address: string | null
  city: string | null
  state: string | null
  country: string | null
  status: HotelStatus
  createdAt: string
  updatedAt: string
}

export interface HotelFormValues {
  name: string
  description?: string
  address?: string
  city?: string
  state?: string
  country?: string
}

export interface Room {
  id: string
  hotelId: string
  roomType: string
  pricePerNight: number
  capacity: number
  totalRooms: number
  status: RoomStatus
  createdAt: string
  updatedAt: string
}

export interface RoomFormValues {
  roomType: string
  pricePerNight: number
  capacity: number
  totalRooms: number
}

export interface RoomAvailability {
  roomId: string
  startDate: string
  endDate: string
  totalRooms: number
  reservedRooms: number
  availableRooms: number
}

export interface RoomReservation {
  id: string
  roomId: string
  tourId: string
  tourLegId: string | null
  reservedBy: string
  startDate: string
  endDate: string
  roomsReserved: number
  status: ReservationStatus
  createdAt: string
  updatedAt: string
}
