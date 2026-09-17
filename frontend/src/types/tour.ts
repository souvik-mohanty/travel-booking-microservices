// Mirrors backend/services/tour-service's TourResponse record.
export type TourStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED'

export interface Tour {
  id: string
  title: string
  description: string | null
  destination: string
  startDate: string
  endDate: string
  price: number
  maxParticipants: number
  status: TourStatus
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface CreateTourRequest {
  title: string
  description?: string
  destination: string
  startDate: string
  endDate: string
  price: number
  maxParticipants: number
}
