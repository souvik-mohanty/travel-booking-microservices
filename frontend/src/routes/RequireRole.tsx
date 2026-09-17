import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { UserRole } from '../types/auth'

// Client-side mirror of a real server-side gate (e.g. tour-service's
// POST /api/tours -> hasRole("BUSINESS")) -- this only avoids showing a
// tourist a form that would 403; the backend is what actually enforces it.
export function RequireRole({ role, children }: { role: UserRole; children: ReactNode }) {
  const { user } = useAuth()

  if (user?.role !== role) {
    return <Navigate to="/tours" replace />
  }

  return <>{children}</>
}
