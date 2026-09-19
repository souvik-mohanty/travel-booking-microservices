import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import type { UserRole } from '@/types/auth'

// Client-side mirror of a real server-side gate (e.g. catalog-service's
// POST /api/tours -> hasRole("BUSINESS")) -- this only avoids showing someone a
// page whose actions would 403; the backend is what actually enforces it.
export function RequireRole({ roles, children }: { roles: UserRole[]; children: ReactNode }) {
  const user = useAuthStore((state) => state.user)

  if (!user || !roles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />
  }

  return <>{children}</>
}
