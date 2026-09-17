// Mirrors backend/services/tour-service's TourLeg/TourActivity responses.
export interface TourLeg {
  id: string
  tourId: string
  sequenceOrder: number
  destination: string
  startDate: string
  endDate: string
  hotelId: string
  roomId: string
  roomsBooked: number
  createdAt: string
}

export interface CreateTourLegRequest {
  destination?: string
  startDate: string
  endDate: string
  hotelId: string
  roomId: string
  roomsBooked: number
}

export interface TourActivityLink {
  id: string
  tourId: string
  activityId: string
  scheduledDate: string
  scheduledTime: string | null
  createdAt: string
}

export interface AddTourActivityRequest {
  activityId: string
  scheduledDate: string
  scheduledTime?: string
}
