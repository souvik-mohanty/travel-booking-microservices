import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import type { Booking, CreateBookingRequest } from '@/types/booking'

async function createBooking(request: CreateBookingRequest): Promise<Booking> {
  const { data } = await apiClient.post<Booking>('/api/bookings', request)
  return data
}

async function getMyBookings(): Promise<Booking[]> {
  const { data } = await apiClient.get<Booking[]>('/api/bookings')
  return data
}

async function getBooking(id: string): Promise<Booking> {
  const { data } = await apiClient.get<Booking>(`/api/bookings/${id}`)
  return data
}

async function cancelBooking(id: string): Promise<Booking> {
  const { data } = await apiClient.patch<Booking>(`/api/bookings/${id}/cancel`)
  return data
}

async function getBookingsForTour(tourId: string): Promise<Booking[]> {
  const { data } = await apiClient.get<Booking[]>(`/api/bookings/tours/${tourId}`)
  return data
}

export const bookingKeys = {
  mine: ['bookings', 'mine'] as const,
  detail: (id: string) => ['bookings', id] as const,
  forTour: (tourId: string) => ['bookings', 'tour', tourId] as const,
}

export function useMyBookingsQuery() {
  return useQuery({ queryKey: bookingKeys.mine, queryFn: getMyBookings })
}

export function useBookingQuery(id: string | undefined) {
  return useQuery({
    queryKey: bookingKeys.detail(id ?? ''),
    queryFn: () => getBooking(id as string),
    enabled: Boolean(id),
  })
}

export function useBookingsForTourQuery(tourId: string | undefined) {
  return useQuery({
    queryKey: bookingKeys.forTour(tourId ?? ''),
    queryFn: () => getBookingsForTour(tourId as string),
    enabled: Boolean(tourId),
  })
}

export function useCreateBookingMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createBooking,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: bookingKeys.mine }),
  })
}

export function useCancelBookingMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: cancelBooking,
    onSuccess: (booking) => {
      queryClient.invalidateQueries({ queryKey: bookingKeys.mine })
      queryClient.setQueryData(bookingKeys.detail(booking.id), booking)
    },
  })
}
