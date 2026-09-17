import { tourClient } from './client'
import type { CreateTourRequest, Tour } from '../types/tour'

export async function getTour(id: string): Promise<Tour> {
  const { data } = await tourClient.get<Tour>(`/api/tours/${id}`)
  return data
}

// tour-service has no "my tours" endpoint -- GET /api/tours returns every
// tour system-wide (every status, every creator), so callers that need just
// one creator's tours filter client-side (see MyToursPage).
export async function getAllTours(): Promise<Tour[]> {
  const { data } = await tourClient.get<Tour[]>('/api/tours')
  return data
}

export async function createTour(request: CreateTourRequest): Promise<Tour> {
  const { data } = await tourClient.post<Tour>('/api/tours', request)
  return data
}

export async function publishTour(id: string): Promise<Tour> {
  const { data } = await tourClient.patch<Tour>(`/api/tours/${id}/publish`)
  return data
}

export async function cancelTour(id: string): Promise<Tour> {
  const { data } = await tourClient.patch<Tour>(`/api/tours/${id}/cancel`)
  return data
}
