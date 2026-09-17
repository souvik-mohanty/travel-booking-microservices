import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import { clearSession, decodeAccessToken, loadSession, saveSession } from '../api/tokenStorage'
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../types/auth'

interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<void>
  register: (request: RegisterRequest) => Promise<void>
  logout: () => Promise<void>
  applySession: (auth: Pick<AuthResponse, 'accessToken' | 'refreshToken'>) => void
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
    setUser(decodeAccessToken(auth.accessToken))
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      applySession,
      login: async (request) => {
        const auth = await authApi.login(request)
        applySession(auth)
      },
      register: async (request) => {
        const auth = await authApi.register(request)
        applySession(auth)
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
