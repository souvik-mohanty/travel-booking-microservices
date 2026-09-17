import { searchClient } from './client'
import type { TourSearchParams, TourSearchResultPage } from '../types/search'

export async function searchTours(params: TourSearchParams): Promise<TourSearchResultPage> {
  const { data } = await searchClient.get<TourSearchResultPage>('/api/search/tours', {
    params: {
      q: params.q || undefined,
      destination: params.destination || undefined,
      minPrice: params.minPrice ?? undefined,
      maxPrice: params.maxPrice ?? undefined,
      page: params.page ?? 0,
      size: params.size ?? 20,
    },
  })
  return data
}
