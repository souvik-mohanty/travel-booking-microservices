// Mirrors engagement-service's ReviewResponse.
export type ReviewStatus = 'ACTIVE' | 'HIDDEN' | 'DELETED'

export interface Review {
  id: string
  bookingId: string
  businessId: string
  activityId: string
  userId: string
  rating: number
  title: string | null
  comment: string
  status: ReviewStatus
  createdAt: string
  updatedAt: string
}

export interface CreateReviewRequest {
  bookingId: string
  businessId: string
  activityId: string
  rating: number
  title?: string
  comment: string
}

export interface UpdateReviewRequest {
  rating?: number
  title?: string
  comment?: string
}
