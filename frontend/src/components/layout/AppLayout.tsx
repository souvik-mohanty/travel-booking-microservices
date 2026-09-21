import type { ReactNode } from 'react'
import { NavLink, useLocation, useNavigate } from 'react-router-dom'
import { logout as logoutApi } from '@/features/auth/api'
import { Logo } from '@/components/common/Logo'
import { useAuthStore } from '@/store/authStore'
import { homePathForRole } from '@/lib/roles'
import type { UserRole } from '@/types/auth'

const TOURIST_NAV_ITEMS = [
  { to: '/tours', label: 'Explore Tours' },
  { to: '/destinations', label: 'Destinations' },
  { to: '/bookings', label: 'My Bookings' },
]

const BUSINESS_NAV_ITEMS = [
  { to: '/my-tours', label: 'My Tours' },
  { to: '/business', label: 'My Business' },
  { to: '/hotels', label: 'My Hotels' },
  { to: '/tours/new', label: 'Create Tour' },
  { to: '/tours', label: 'Explore Tours' },
]

const ADMIN_NAV_ITEMS = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/businesses', label: 'Businesses' },
  { to: '/admin/hotels', label: 'Hotels' },
  { to: '/tours', label: 'Explore Tours' },
]

const ROLE_LABELS: Record<UserRole, string> = {
  TOURIST: 'Tourist',
  BUSINESS: 'Business',
  ADMIN: 'Admin',
}

const NAV_ITEMS_BY_ROLE: Record<UserRole, typeof TOURIST_NAV_ITEMS> = {
  TOURIST: TOURIST_NAV_ITEMS,
  BUSINESS: BUSINESS_NAV_ITEMS,
  ADMIN: ADMIN_NAV_ITEMS,
}

export function AppLayout({ children }: { children: ReactNode }) {
  const user = useAuthStore((state) => state.user)
  const tokens = useAuthStore((state) => state.tokens)
  const storeLogout = useAuthStore((state) => state.logout)
  const navigate = useNavigate()
  const location = useLocation()
  const navItems = NAV_ITEMS_BY_ROLE[user?.role ?? 'TOURIST']
  const homeLink = homePathForRole(user?.role)

  async function handleLogout() {
    if (tokens?.refreshToken) {
      await logoutApi(tokens.refreshToken).catch(() => {
        // Best-effort server-side revocation -- always clear local state.
      })
    }
    storeLogout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <header className="relative border-b border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
        {/* Brand accent line -- the one place the blue->orange gradient always shows, unmissable but not loud. */}
        <div className="tf-gradient-bar absolute inset-x-0 top-0 h-0.5" />

        <div className="mx-auto flex max-w-6xl items-center justify-between gap-6 px-6 py-4">
          <div className="flex items-center gap-8">
            <NavLink
              to={homeLink}
              aria-label="TourFlow home"
              className="transition-transform duration-200 hover:scale-[1.03]"
            >
              <Logo size={40} />
            </NavLink>
            <nav className="hidden gap-5 text-sm md:flex">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `relative py-1 transition-colors duration-200 after:absolute after:-bottom-[1px] after:left-0 after:h-0.5 after:rounded-full after:transition-all after:duration-300 ${
                      isActive
                        ? 'font-medium text-blue-700 after:w-full after:bg-blue-600 dark:text-blue-400 dark:after:bg-blue-400'
                        : 'text-slate-500 after:w-0 after:bg-orange-500 hover:text-slate-900 hover:after:w-full dark:text-slate-400 dark:hover:text-slate-100'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-4">
            {user && (
              <span className="hidden items-center gap-2 text-sm text-slate-500 dark:text-slate-400 sm:flex">
                {user.email}
                <span className="rounded-full bg-orange-100 px-2 py-0.5 text-xs font-medium text-orange-700 dark:bg-orange-950 dark:text-orange-300">
                  {ROLE_LABELS[user.role]}
                </span>
              </span>
            )}
            <button
              onClick={handleLogout}
              className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
            >
              Sign out
            </button>
          </div>
        </div>

        <nav className="flex gap-4 overflow-x-auto border-t border-slate-100 px-6 py-2 text-sm md:hidden dark:border-slate-800">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                isActive
                  ? 'whitespace-nowrap font-medium text-blue-700 dark:text-blue-400'
                  : 'whitespace-nowrap text-slate-500 dark:text-slate-400'
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main key={location.pathname} className="mx-auto max-w-6xl px-6 py-8 animate-fade-in-up">
        {children}
      </main>
    </div>
  )
}
