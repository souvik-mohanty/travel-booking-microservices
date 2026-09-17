import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import { clearSession, decodeAccessToken, loadSession, saveSession } from '../api/tokenStorage'
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../types/auth'

interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  // Return the freshly-decoded user directly, rather than making the caller
  // wait for a re-render to read the new value off context -- LoginPage
  // needs the role immediately to pick a redirect target.
  login: (request: LoginRequest) => Promise<AuthUser | null>
  register: (request: RegisterRequest) => Promise<AuthUser | null>
  logout: () => Promise<void>
  applySession: (auth: Pick<AuthResponse, 'accessToken' | 'refreshToken'>) => AuthUser | null
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function userFromStoredSession(): AuthUser | null {
  const session = loadSession()
  return session ? decodeAccessToken(session.accessToken) : null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(userFromStoredSession)

  const applySession = (auth: Pick<AuthResponse, 'accessToken' | 'refreshToken'>) => {
    saveSession(auth)
    const decoded = decodeAccessToken(auth.accessToken)
    setUser(decoded)
    return decoded
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      applySession,
      login: async (request) => {
        const auth = await authApi.login(request)
        return applySession(auth)
      },
      register: async (request) => {
        const auth = await authApi.register(request)
        return applySession(auth)
      },
      logout: async () => {
        const session = loadSession()
        if (session?.refreshToken) {
          await authApi.logout(session.refreshToken).catch(() => {
            // Best-effort server-side revocation -- always clear local state.
          })
        }
        clearSession()
        setUser(null)
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within an AuthProvider')
  return context
}
