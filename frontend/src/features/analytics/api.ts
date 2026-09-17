import { useQuery } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import type { BookingSummary, DailyBookingStats, TourBookingStats } from '@/types/analytics'

async function getBookingSummary(): Promise<BookingSummary> {
  const { data } = await apiClient.get<BookingSummary>('/api/analytics/bookings/summary')
  return data
}

async function getDailyBookingStats(): Promise<DailyBookingStats[]> {
  const { data } = await apiClient.get<DailyBookingStats[]>('/api/analytics/bookings/daily')
  return data
}

// No {tourId} path variable exists on this endpoint -- it returns every
// tour's stats grouped, not one tour's (verified against insights-service's
// actual controller, contradicting the older docs).
async function getStatsByTour(): Promise<TourBookingStats[]> {
  const { data } = await apiClient.get<TourBookingStats[]>('/api/analytics/bookings/by-tour')
  return data
}

export function useBookingSummaryQuery() {
  return useQuery({ queryKey: ['analytics', 'summary'], queryFn: getBookingSummary })
}

export function useDailyBookingStatsQuery() {
  return useQuery({ queryKey: ['analytics', 'daily'], queryFn: getDailyBookingStats })
}

export function useStatsByTourQuery() {
  return useQuery({ queryKey: ['analytics', 'by-tour'], queryFn: getStatsByTour })
}
