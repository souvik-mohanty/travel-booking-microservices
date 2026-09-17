import { apiClient, API_BASE_URL } from '@/lib/apiClient'
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/login', request)
  return data
}

export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/register', request)
  return data
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post('/api/auth/logout', { refreshToken })
}

// Full-page navigation, not an XHR -- Google's OAuth2 handshake needs a real
// browser redirect through identity-service and back.
export function startGoogleLogin(): void {
  window.location.assign(`${API_BASE_URL}/oauth2/authorization/google`)
}
