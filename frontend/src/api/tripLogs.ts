import { tripClient } from './client'
import type { CreateTripLogEntryRequest, TripLogEntry } from '../types/trip'

export async function getTripLog(tripId: string): Promise<TripLogEntry[]> {
  const { data } = await tripClient.get<TripLogEntry[]>(`/api/trips/${tripId}/log`)
  return data
}

export async function addTripLogEntry(
  tripId: string,
  request: CreateTripLogEntryRequest,
): Promise<TripLogEntry> {
  const { data } = await tripClient.post<TripLogEntry>(`/api/trips/${tripId}/log`, request)
  return data
}
