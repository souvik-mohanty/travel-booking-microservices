import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { clearSession, loadSession, saveSession } from './tokenStorage'

// api-gateway has no routes configured yet (see backend/gateway/api-gateway),
// so the frontend talks to each backend service directly on its own port.
export const SERVICE_URLS = {
  identity: import.meta.env.VITE_IDENTITY_SERVICE_URL ?? 'http://localhost:8081',
  tour: import.meta.env.VITE_TOUR_SERVICE_URL ?? 'http://localhost:8082',
  booking: import.meta.env.VITE_BOOKING_SERVICE_URL ?? 'http://localhost:8083',
  search: import.meta.env.VITE_SEARCH_SERVICE_URL ?? 'http://localhost:8099',
} as const

// Only identity-service issues/refreshes tokens -- every other client's 401
// handler still refreshes through here, not through itself.
const AUTH_ENDPOINTS = ['/api/auth/login', '/api/auth/register', '/api/auth/refresh']

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const session = loadSession()
  if (!session?.refreshToken) return null

  if (!refreshPromise) {
    refreshPromise = axios
      .post<{ accessToken: string; refreshToken: string }>(
        `${SERVICE_URLS.identity}/api/auth/refresh`,
        { refreshToken: session.refreshToken },
      )
      .then(({ data }) => {
        saveSession(data)
        return data.accessToken
      })
      .catch(() => {
        clearSession()
        return null
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

function createServiceClient(baseURL: string): AxiosInstance {
  const client = axios.create({ baseURL })

  client.interceptors.request.use((config) => {
    const session = loadSession()
    if (session?.accessToken) {
      config.headers.set('Authorization', `Bearer ${session.accessToken}`)
    }
    return config
  })

  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as (InternalAxiosRequestConfig & { _retried?: boolean }) | undefined
      const isAuthEndpoint = AUTH_ENDPOINTS.some((path) => originalRequest?.url?.includes(path))

      if (error.response?.status === 401 && originalRequest && !originalRequest._retried && !isAuthEndpoint) {
        originalRequest._retried = true
        const newAccessToken = await refreshAccessToken()

        if (newAccessToken) {
          originalRequest.headers.set('Authorization', `Bearer ${newAccessToken}`)
          return client(originalRequest)
        }

        window.location.assign('/login')
      }

      return Promise.reject(error)
    },
  )

  return client
}

export const identityClient = createServiceClient(SERVICE_URLS.identity)
export const tourClient = createServiceClient(SERVICE_URLS.tour)
export const bookingClient = createServiceClient(SERVICE_URLS.booking)
export const searchClient = createServiceClient(SERVICE_URLS.search)
