import { analyticsClient } from './client'
import type { BookingSummary, DailyBookingStats, TourBookingStats } from '../types/analytics'

export async function getBookingSummary(): Promise<BookingSummary> {
  const { data } = await analyticsClient.get<BookingSummary>('/api/analytics/bookings/summary')
  return data
}

export async function getDailyBookingStats(from?: string, to?: string): Promise<DailyBookingStats[]> {
  const { data } = await analyticsClient.get<DailyBookingStats[]>('/api/analytics/bookings/daily', {
    params: { from, to },
  })
  return data
}

export async function getStatsByTour(): Promise<TourBookingStats[]> {
  const { data } = await analyticsClient.get<TourBookingStats[]>('/api/analytics/bookings/by-tour')
  return data
}
