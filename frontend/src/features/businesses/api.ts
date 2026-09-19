import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import type { Business, BusinessFormValues } from '@/types/business'

// catalog-service has no "my business" endpoint -- GET /api/businesses returns
// every business system-wide, so callers filter by ownerId client-side.
async function getAllBusinesses(): Promise<Business[]> {
  const { data } = await apiClient.get<Business[]>('/api/businesses')
  return data
}

async function createBusiness(request: BusinessFormValues): Promise<Business> {
  const { data } = await apiClient.post<Business>('/api/businesses', request)
  return data
}

// A full replace, not a partial patch: the backend requires every field.
async function updateBusiness({ id, request }: { id: string; request: BusinessFormValues }): Promise<Business> {
  const { data } = await apiClient.put<Business>(`/api/businesses/${id}`, request)
  return data
}

// Admin moderation -- gated to the ADMIN role server-side.
async function suspendBusiness(id: string): Promise<Business> {
  const { data } = await apiClient.patch<Business>(`/api/businesses/${id}/suspend`)
  return data
}

async function reinstateBusiness(id: string): Promise<Business> {
  const { data } = await apiClient.patch<Business>(`/api/businesses/${id}/reinstate`)
  return data
}

export const businessKeys = { all: ['businesses'] as const }

export function useAllBusinessesQuery() {
  return useQuery({ queryKey: businessKeys.all, queryFn: getAllBusinesses })
}

function useInvalidatingMutation<TVars>(mutationFn: (vars: TVars) => Promise<Business>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: businessKeys.all }),
  })
}

export const useCreateBusinessMutation = () => useInvalidatingMutation(createBusiness)
export const useUpdateBusinessMutation = () => useInvalidatingMutation(updateBusiness)
export const useSuspendBusinessMutation = () => useInvalidatingMutation(suspendBusiness)
export const useReinstateBusinessMutation = () => useInvalidatingMutation(reinstateBusiness)
