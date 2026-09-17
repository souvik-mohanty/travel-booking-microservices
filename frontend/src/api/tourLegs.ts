import { tourClient } from './client'
import type { CreateTourLegRequest, TourLeg } from '../types/itinerary'

export async function getLegsForTour(tourId: string): Promise<TourLeg[]> {
  const { data } = await tourClient.get<TourLeg[]>(`/api/tours/${tourId}/legs`)
  return data
}

export async function addLeg(tourId: string, request: CreateTourLegRequest): Promise<TourLeg> {
  const { data } = await tourClient.post<TourLeg>(`/api/tours/${tourId}/legs`, request)
  return data
}

export async function removeLeg(tourId: string, legId: string): Promise<void> {
  await tourClient.delete(`/api/tours/${tourId}/legs/${legId}`)
}
