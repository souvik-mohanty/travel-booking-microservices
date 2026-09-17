import { businessClient } from './client'
import type { Business, BusinessFormValues } from '../types/business'

export async function getBusiness(id: string): Promise<Business> {
  const { data } = await businessClient.get<Business>(`/api/businesses/${id}`)
  return data
}

// business-service has no "my business" endpoint -- GET /api/businesses
// returns every business system-wide, so callers filter client-side (see
// MyBusinessPage), same pattern as tour-service's getAllTours.
export async function getAllBusinesses(): Promise<Business[]> {
  const { data } = await businessClient.get<Business[]>('/api/businesses')
  return data
}

export async function createBusiness(request: BusinessFormValues): Promise<Business> {
  const { data } = await businessClient.post<Business>('/api/businesses', request)
  return data
}

export async function updateBusiness(id: string, request: BusinessFormValues): Promise<Business> {
  const { data } = await businessClient.put<Business>(`/api/businesses/${id}`, request)
  return data
}

// Admin moderation -- gated to the ADMIN role server-side.
export async function suspendBusiness(id: string): Promise<Business> {
  const { data } = await businessClient.patch<Business>(`/api/businesses/${id}/suspend`)
  return data
}

export async function reinstateBusiness(id: string): Promise<Business> {
  const { data } = await businessClient.patch<Business>(`/api/businesses/${id}/reinstate`)
  return data
}
