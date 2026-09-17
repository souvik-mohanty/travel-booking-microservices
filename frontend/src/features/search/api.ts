import { useQuery } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import type { TourSearchParams, TourSearchResultPage } from '@/types/search'

async function searchTours(params: TourSearchParams): Promise<TourSearchResultPage> {
  const { data } = await apiClient.get<TourSearchResultPage>('/api/search/tours', { params })
  return data
}

export function useTourSearchQuery(params: TourSearchParams) {
  return useQuery({
    queryKey: ['search', 'tours', params],
    queryFn: () => searchTours(params),
    placeholderData: (previous) => previous,
  })
}
