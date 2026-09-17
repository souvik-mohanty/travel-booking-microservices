import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
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

apiClient.interceptors.request.use((config) => {
  const { tokens } = useAuthStore.getState()
  if (tokens?.accessToken) {
    config.headers.set('Authorization', `Bearer ${tokens.accessToken}`)
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
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
