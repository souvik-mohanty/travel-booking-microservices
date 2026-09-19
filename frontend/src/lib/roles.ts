import type { UserRole } from '@/types/auth'

// Where each role lands after signing in, and what "home" means in the nav.
// Only points at pages that exist: the business portal (My Business / My Hotels)
// and admin management tables are not built yet, so a business account's home
// is its tour list and an admin's is the analytics dashboard.
export function homePathForRole(role: UserRole | undefined): string {
  switch (role) {
    case 'ADMIN':
      return '/admin'
    case 'BUSINESS':
      return '/my-tours'
    default:
      return '/tours'
  }
}
