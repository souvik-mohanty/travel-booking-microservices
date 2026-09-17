import { identityClient, SERVICE_URLS } from './client'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await identityClient.post<AuthResponse>('/api/auth/login', request)
  return data
}

export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await identityClient.post<AuthResponse>('/api/auth/register', request)
  return data
}

export async function logout(refreshToken: string): Promise<void> {
  await identityClient.post('/api/auth/logout', { refreshToken })
}

// Starts Spring Security's own OAuth2 handshake (SecurityConfig permits this
// path unauthenticated). It's a full browser navigation, not an XHR: Google
// needs to redirect the top-level page, and the flow ends back at
// /oauth2/redirect (see OAuth2AuthenticationSuccessHandler).
export function startGoogleLogin(): void {
  window.location.assign(`${SERVICE_URLS.identity}/oauth2/authorization/google`)
}
