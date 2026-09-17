// Mirrors backend/services/search-service's TourSearchResponse/TourSearchResultResponse.
export interface TourSearchResult {
  id: string
  title: string
  description: string | null
  destination: string
  startDate: string
  endDate: string
  price: number
  maxParticipants: number
  createdBy: string
}

export interface TourSearchResultPage {
  results: TourSearchResult[]
  totalResults: number
  page: number
  size: number
}

export interface TourSearchParams {
  q?: string
  destination?: string
  minPrice?: number
  maxPrice?: number
  page?: number
  size?: number
}
