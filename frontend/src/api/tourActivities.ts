import { tourClient } from './client'
import type { AddTourActivityRequest, TourActivityLink } from '../types/itinerary'

export async function getActivitiesForTour(tourId: string): Promise<TourActivityLink[]> {
  const { data } = await tourClient.get<TourActivityLink[]>(`/api/tours/${tourId}/activities`)
  return data
}

export async function addActivityToTour(tourId: string, request: AddTourActivityRequest): Promise<TourActivityLink> {
  const { data } = await tourClient.post<TourActivityLink>(`/api/tours/${tourId}/activities`, request)
  return data
}

export async function removeActivityFromTour(tourId: string, activityId: string): Promise<void> {
  await tourClient.delete(`/api/tours/${tourId}/activities/${activityId}`)
}
