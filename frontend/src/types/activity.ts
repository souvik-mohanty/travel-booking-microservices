// Mirrors backend/services/business-service's Activity/ActivityResponse.
export type ActivityCategory = 'ADVENTURE' | 'SIGHTSEEING' | 'TRANSPORT' | 'OTHER'
export type ActivityStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'DELETED'

export interface Activity {
  id: string
  businessId: string
  name: string
  description: string | null
  location: string | null
  price: number
  durationMinutes: number
  maxParticipants: number
  category: ActivityCategory
  status: ActivityStatus
  createdAt: string
  updatedAt: string
}

export interface ActivityFormValues {
  name: string
  description?: string
  location?: string
  price: number
  durationMinutes: number
  maxParticipants: number
  category: ActivityCategory
}
