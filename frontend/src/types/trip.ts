// Mirrors backend/services/trip-service's Trip/TripLogEntry responses.
export type TripStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type TripLogEntryType = 'CHECK_IN' | 'CHECK_OUT' | 'ACTIVITY_COMPLETED' | 'DESTINATION_REACHED' | 'OTHER'

export interface Trip {
  id: string
  bookingId: string
  createdBy: string
  driverId: string | null
  vehicleId: string | null
  status: TripStatus
  startedAt: string | null
  completedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface TripLogEntry {
  id: string
  tripId: string
  type: TripLogEntryType
  description: string | null
  location: string | null
  occurredAt: string
  loggedBy: string
  createdAt: string
}

export interface CreateTripLogEntryRequest {
  type: TripLogEntryType
  description?: string
  location?: string
  occurredAt?: string
}
