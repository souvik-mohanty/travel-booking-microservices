import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import type { CreateTourRequest, Tour } from '@/types/tour'

async function getTour(id: string): Promise<Tour> {
  const { data } = await apiClient.get<Tour>(`/api/tours/${id}`)
  return data
}

async function getAllTours(): Promise<Tour[]> {
  const { data } = await apiClient.get<Tour[]>('/api/tours')
  return data
}

async function createTour(request: CreateTourRequest): Promise<Tour> {
  const { data } = await apiClient.post<Tour>('/api/tours', request)
  return data
}

async function publishTour(id: string): Promise<Tour> {
  const { data } = await apiClient.patch<Tour>(`/api/tours/${id}/publish`)
  return data
}

async function cancelTour(id: string): Promise<Tour> {
  const { data } = await apiClient.patch<Tour>(`/api/tours/${id}/cancel`)
  return data
}

export const tourKeys = {
  all: ['tours'] as const,
  detail: (id: string) => ['tours', id] as const,
}

export function useTourQuery(id: string | undefined) {
  return useQuery({
    queryKey: tourKeys.detail(id ?? ''),
    queryFn: () => getTour(id as string),
    enabled: Boolean(id),
  })
}

export function useAllToursQuery() {
  return useQuery({ queryKey: tourKeys.all, queryFn: getAllTours })
}

export function useCreateTourMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createTour,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: tourKeys.all }),
  })
}

export function usePublishTourMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: publishTour,
    onSuccess: (tour) => {
      queryClient.invalidateQueries({ queryKey: tourKeys.all })
      queryClient.setQueryData(tourKeys.detail(tour.id), tour)
    },
  })
}

export function useCancelTourMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: cancelTour,
    onSuccess: (tour) => {
      queryClient.invalidateQueries({ queryKey: tourKeys.all })
      queryClient.setQueryData(tourKeys.detail(tour.id), tour)
    },
  })
}
