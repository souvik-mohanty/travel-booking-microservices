import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { toast } from 'sonner'
import { useAuthStore } from '@/store/authStore'

// Every backend path (auth, tours, bookings, payments, reviews, tickets,
// notifications, analytics, ...) is reachable through the api-gateway with
// no path rewriting -- see backend/gateway/api-gateway. This is also
// required, not just convenient: engagement-service and platform-service
// have no CORS bean configured, so calling them directly from a browser
// (bypassing the gateway) would fail outright.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const apiClient = axios.create({ baseURL: API_BASE_URL })

// A separate, non-intercepted instance used only for the refresh call
// itself, so a failed refresh can never recursively trigger this same 401
// handler.
const rawClient = axios.create({ baseURL: API_BASE_URL })

const AUTH_ENDPOINTS = ['/api/auth/login', '/api/auth/register', '/api/auth/refresh']

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const { tokens } = useAuthStore.getState()
  if (!tokens?.refreshToken) return null

  if (!refreshPromise) {
    refreshPromise = rawClient
      .post<{ accessToken: string; refreshToken: string }>('/api/auth/refresh', {
        refreshToken: tokens.refreshToken,
      })
      .then(({ data }) => {
        useAuthStore.getState().setSession(data)
        return data.accessToken
      })
      .catch(() => {
        useAuthStore.getState().logout()
        return null
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

// Free-tier hosting (Render) sleeps a service after 15 minutes idle and can
// take up to ~a minute to wake back up on the next request -- without this,
// a cold hit just looks like the app hanging. Only shown once across however
// many requests are slow at once (a dashboard firing several queries during
// a cold start shouldn't stack up several toasts), and only if a request is
// still pending after SLOW_REQUEST_MS, so it never fires on a normal warm hit.
type ColdStartTrackedConfig = InternalAxiosRequestConfig & {
  _slowTimer?: ReturnType<typeof setTimeout>
  _slowTriggered?: boolean
}
const SLOW_REQUEST_MS = 4000
const COLD_START_TOAST_ID = 'cold-start'
let slowRequestCount = 0

function clearColdStartTracking(config?: ColdStartTrackedConfig) {
  if (!config) return
  clearTimeout(config._slowTimer)
  if (config._slowTriggered) {
    slowRequestCount = Math.max(0, slowRequestCount - 1)
    if (slowRequestCount === 0) {
      toast.dismiss(COLD_START_TOAST_ID)
    }
  }
}

apiClient.interceptors.request.use((config: ColdStartTrackedConfig) => {
  const { tokens } = useAuthStore.getState()
  if (tokens?.accessToken) {
    config.headers.set('Authorization', `Bearer ${tokens.accessToken}`)
  }
  config._slowTimer = setTimeout(() => {
    config._slowTriggered = true
    slowRequestCount += 1
    toast.loading('Waking up the server… this can take up to a minute on free-tier hosting.', {
      id: COLD_START_TOAST_ID,
    })
  }, SLOW_REQUEST_MS)
  return config
})

apiClient.interceptors.response.use(
  (response) => {
    clearColdStartTracking(response.config as ColdStartTrackedConfig)
    return response
  },
  async (error: AxiosError) => {
    clearColdStartTracking(error.config as ColdStartTrackedConfig)
    const originalRequest = error.config as (InternalAxiosRequestConfig & { _retried?: boolean }) | undefined
    const isAuthEndpoint = AUTH_ENDPOINTS.some((path) => originalRequest?.url?.includes(path))

    if (error.response?.status === 401 && originalRequest && !originalRequest._retried && !isAuthEndpoint) {
      originalRequest._retried = true
      const newAccessToken = await refreshAccessToken()

      if (newAccessToken) {
        originalRequest.headers.set('Authorization', `Bearer ${newAccessToken}`)
        return apiClient(originalRequest)
      }
      // Refresh failed -- useAuthStore.logout() already ran inside
      // refreshAccessToken's catch. Any component reading isAuthenticated
      // (ProtectedRoute) re-renders and redirects on its own; the client
      // stays decoupled from the router here.
    }

    return Promise.reject(error)
  },
)
