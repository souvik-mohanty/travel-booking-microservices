import type { AuthResponse, AuthUser, UserRole } from '../types/auth'

const STORAGE_KEY = 'tourflow.auth'

interface StoredSession {
  accessToken: string
  refreshToken: string
}

export function saveSession(auth: Pick<AuthResponse, 'accessToken' | 'refreshToken'>): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(auth satisfies StoredSession))
}

export function loadSession(): StoredSession | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredSession
  } catch {
    return null
  }
}

export function clearSession(): void {
  localStorage.removeItem(STORAGE_KEY)
}

// The access token's payload (userId/email/role) is enough to render the UI
// without a dedicated GET /me endpoint (identity-service doesn't expose one).
// This only decodes the claims for display -- the backend is the one place
// that verifies the signature.
export function decodeAccessToken(accessToken: string): AuthUser | null {
  const parts = accessToken.split('.')
  if (parts.length !== 3) return null

  try {
    const payload = JSON.parse(base64UrlDecode(parts[1])) as {
      sub: string
      email: string
      role: string
    }
    const role: UserRole =
      payload.role === 'BUSINESS' || payload.role === 'ADMIN' ? payload.role : 'TOURIST'
    return { userId: payload.sub, email: payload.email, role }
  } catch {
    return null
  }
}

function base64UrlDecode(segment: string): string {
  const base64 = segment.replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
  return decodeURIComponent(
    atob(padded)
      .split('')
      .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
      .join(''),
  )
}
