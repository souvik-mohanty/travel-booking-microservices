import { QueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'

// React Query must never retry a 401 itself -- that's apiClient's
// interceptor's job alone (it dedupes concurrent refreshes and retries the
// original request once). Retrying here too would race the interceptor and
// could trigger duplicate refresh calls.
function shouldRetry(failureCount: number, error: unknown): boolean {
  if (error instanceof AxiosError && error.response?.status === 401) return false
  return failureCount < 2
}

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: shouldRetry,
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
})
