import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthResponse, AuthUser, UserRole } from '@/types/auth'

interface StoredTokens {
  accessToken: string
  refreshToken: string
}

interface AuthState {
  user: AuthUser | null
  tokens: StoredTokens | null
  isAuthenticated: boolean
  // Returns the freshly-decoded user directly, rather than making the caller
  // wait for a re-render to read the new value off the store -- LoginPage
  // needs the role immediately to pick a redirect target.
  setSession: (auth: Pick<AuthResponse, 'accessToken' | 'refreshToken'>) => AuthUser | null
  logout: () => void
}

// The access token's payload (userId/email/role) is enough to render the UI
// without a dedicated GET /me endpoint (identity-service doesn't expose one).
// This only decodes the claims for display -- the backend is the one place
// that verifies the signature.
function decodeAccessToken(accessToken: string): AuthUser | null {
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

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      tokens: null,
      isAuthenticated: false,
      setSession: (auth) => {
        const user = decodeAccessToken(auth.accessToken)
        set({
          user,
          tokens: { accessToken: auth.accessToken, refreshToken: auth.refreshToken },
          isAuthenticated: user !== null,
        })
        return user
      },
      logout: () => set({ user: null, tokens: null, isAuthenticated: false }),
    }),
    {
      name: 'tourflow.auth',
    },
  ),
)
