// Mirrors backend/services/business-service's BusinessResponse record.
export type BusinessStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE'

export interface Business {
  id: string
  ownerId: string
  name: string
  description: string | null
  phone: string | null
  email: string | null
  address: string | null
  city: string | null
  state: string | null
  country: string | null
  status: BusinessStatus
  createdAt: string
  updatedAt: string
}

// Shared by create and update -- business-service's UpdateBusinessRequest
// requires the same full set of fields as CreateBusinessRequest (a full
// replace, not a partial patch).
export interface BusinessFormValues {
  name: string
  description?: string
  phone?: string
  email?: string
  address?: string
  city?: string
  state?: string
  country?: string
}
